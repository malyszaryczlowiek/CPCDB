package com.github.malyszaryczlowiek.cpcdb.tasks;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.TempStability;
import com.github.malyszaryczlowiek.cpcdb.compound.Unit;
import com.github.malyszaryczlowiek.cpcdb.db.ConnectionManager;
import com.github.malyszaryczlowiek.cpcdb.managers.CurrentStatusManager;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ErrorType;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ShortAlertWindowFactory;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class LoadLocalDatabase extends Task<String>
{
    private List<Compound> fullListOfCompounds;
    private ObservableList<Compound> observableList;
    private TableView<Compound> mainSceneTableView;

    /**
     * This method is called from Main JavaFX thread.
     */
    public static Task<String> getTask( List<Compound> fullListOfCompounds, ObservableList<Compound> observableList,
                                        TableView<Compound> mainSceneTableView) {
        LoadLocalDatabase task = new LoadLocalDatabase( fullListOfCompounds, observableList, mainSceneTableView);
        task.setUpTaskListeners();
        return task;
    }

    /**
     * This method is called from Main JavaFX thread.
     */
    private LoadLocalDatabase(List<Compound> fullListOfCompounds, ObservableList<Compound> observableList,
                              TableView<Compound> mainSceneTableView) {
        this.fullListOfCompounds = fullListOfCompounds;
        this.observableList = observableList;
        this.mainSceneTableView = mainSceneTableView;
    }

    /**
     * This method is called from Main JavaFX thread.
     */
    private void setUpTaskListeners() {
        messageProperty().addListener( (observable, oldMessage, newMessage) -> manageNewCommunicates(newMessage) );
        titleProperty().addListener((observable, oldValue, newValue) -> manageNewCommunicates(newValue) );
    }

    /**
     * This method is called from Main JavaFX thread.
     */
    private void manageNewCommunicates(String newMessage) {
        CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
        switch (newMessage) {
            case "cannotConnectToLocalDb":
                ShortAlertWindowFactory.showWindow(ErrorType.CANNOT_CONNECT_TO_LOCAL_DB);
                currentStatusManager.setErrorStatus("Error (Click here for more info)");
                break;
            case "incorrectLocalPassphrase":
                ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_LOCAL_DB_AUTHORISATION);
                currentStatusManager.setErrorStatus("Error - Incorrect UserName or Passphrase");
                break;
            case "reloadingLocalServerDatabaseDone":
                currentStatusManager.setProgressValue(0.0);
                currentStatusManager.setInfoStatus("Reloading Local Server Database Done");
                break;
            case "unknownError":
                ShortAlertWindowFactory.showWindow(ErrorType.UNKNOWN_ERROR_OCCURRED);
                currentStatusManager.setErrorStatus("Unknown Error Occurred");
            default:
                currentStatusManager.setInfoStatus(newMessage);
                break;
        }
    }

    @Override
    protected String call() {
        try (Connection connection = ConnectionManager.connectToLocalDb()) {
            boolean connectedToLocalDb = ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR);
            boolean incorrectLocalPassphrase =
                    ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR);
            if (connectedToLocalDb) updateMessage("cannotConnectToLocalDb");
            else if (incorrectLocalPassphrase) updateMessage("incorrectLocalPassphrase");
            else if ( connection != null) loadTable(connection);
            else updateMessage("unknownError");
        }
        catch ( SQLException e) { e.printStackTrace(); }
        return "Task Ended";
    }

    private void loadTable(Connection connection)  {
        CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
        currentStatusManager.setProgressValue(0.05);
        updateMessage("Connecting to Database...");
        System.err.println("jestem w load table");
        final String numberOfRowsQuery = "SELECT COUNT(*) FROM compounds";
        int size = 0;
        try {
            PreparedStatement loadDBStatement = connection.prepareStatement(numberOfRowsQuery);
            ResultSet resultSet = loadDBStatement.executeQuery();
            while (resultSet.next()) size = resultSet.getInt(1); // getting number of rows
            System.err.println("jestem w load table: SIZE = " + size);
        } catch (SQLException e) { e.printStackTrace(); }
        final String loadDBSQLQuery = "SELECT * FROM compounds";
        try {
            PreparedStatement loadDBStatement = connection.prepareStatement(loadDBSQLQuery);
            updateMessage("Downloading data from Server...");
            ResultSet resultSet = loadDBStatement.executeQuery();
            currentStatusManager.setProgressValue(0.1);
            updateMessage("Loading downloaded data...");
            int index = 0;
            fullListOfCompounds.clear();
            observableList.clear();
            while(resultSet.next()) {
                int id = resultSet.getInt(1);
                String smiles = resultSet.getString(2);
                String compoundNumber = resultSet.getString(3);
                float amount = resultSet.getFloat(4);
                String unit = resultSet.getString(5);
                String form = resultSet.getString(6);
                String tempStability = resultSet.getString(7);
                boolean argon = resultSet.getBoolean(8);
                String container = resultSet.getString(9);
                String storagePlace = resultSet.getString(10);
                LocalDateTime dateTimeModification = resultSet.getTimestamp(11).toLocalDateTime();
                String additionalInformation = resultSet.getString(12);
                Compound compound = new Compound(smiles, compoundNumber, amount, Unit.stringToEnum(unit),
                        form, TempStability.stringToEnum(tempStability), argon, container,
                        storagePlace, dateTimeModification, additionalInformation);
                compound.setId(id);
                compound.setSavedInDatabase(true);
                fullListOfCompounds.add(compound);
                System.err.println( compound.toString() );
                currentStatusManager.addToProgressValue(0.9 * ( 1.0 / ((double) size)));
                if (index % 100 == 0) updateMessage("Loaded "+Math.round((double) ++index/((double) size)*100 )+"%");
            }
            updateMessage("Refreshing table");
            observableList.setAll(fullListOfCompounds);
            mainSceneTableView.setItems(observableList);
        }
        catch (SQLException e) { e.printStackTrace(); }
        updateTitle("reloadingLocalServerDatabaseDone");
    }
}
