package com.github.malyszaryczlowiek.cpcdb.tasks;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.TempStability;
import com.github.malyszaryczlowiek.cpcdb.compound.Unit;
import com.github.malyszaryczlowiek.cpcdb.controllers.MainStageController;
import com.github.malyszaryczlowiek.cpcdb.db.ConnectionManager;
import com.github.malyszaryczlowiek.cpcdb.managers.CurrentStatusManager;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ErrorType;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ShortAlertWindowFactory;

import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class LoadDatabaseFromRemoteWithoutMerging extends Task<Void>
{
    private List<Compound> fullListOfCompounds;
    private ObservableList<Compound> observableList;
    private TableView<Compound> mainSceneTableView;
    private MainStageController mainStageController;

    /**
     * This method is called from Main JavaFX thread.
     */
    public static Task<Void> getTask( List<Compound> fullListOfCompounds,
                                     ObservableList<Compound> observableList, TableView<Compound> mainSceneTableView,
                                     MainStageController mainStageController) {
        LoadDatabaseFromRemoteWithoutMerging task = new LoadDatabaseFromRemoteWithoutMerging(
                         fullListOfCompounds, observableList, mainSceneTableView, mainStageController);
        task.setUpTaskListeners();
        return task;
    }

    /**
     * This method is called from Main JavaFX thread.
     */
    private LoadDatabaseFromRemoteWithoutMerging( List<Compound> fullListOfCompounds,
                                   ObservableList<Compound> observableList, TableView<Compound> mainSceneTableView,
                                                 MainStageController mainStageController) {
        this.fullListOfCompounds = fullListOfCompounds;
        this.observableList = observableList;
        this.mainSceneTableView = mainSceneTableView;
        this.mainStageController = mainStageController;
    }

    /**
     * This method is called from Main JavaFX thread.
     */
    private void setUpTaskListeners() {
        messageProperty().addListener( (observable, oldMessage, newMessage) -> manageNewCommunicates(newMessage) );
        titleProperty().addListener((observable, oldValue, newValue) -> {
            boolean shouldWeConnectToLocalDb = SecureProperties.getProperty("tryToConnectToLocalDb").equals("true");
            CurrentStatusManager.getThisCurrentStatusManager().setProgressValue(0.0);  // if we do not send data to local db we ended work and can reset progress value to 0.0
            if ( shouldWeConnectToLocalDb && newValue.equals("reloadingRemoteServerDatabaseDone") ){
                Task<Void> updatingTask = UpdateLocalDb.getTask(observableList); // here we updating our local server
                Thread updatingLocalDbThread = new Thread(updatingTask);
                updatingLocalDbThread.setDaemon(true);
                updatingLocalDbThread.start();
            }
        });
    }

    @Override
    protected Void call() {
        try (Connection connection = ConnectionManager.reconnectToRemoteDb()) {
            boolean connectedToRemoteDb = ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR);
            boolean incorrectRemotePassphrase = ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR);
            if (connectedToRemoteDb) updateMessage("cannotConnectToRemoteDb");
            if (incorrectRemotePassphrase) updateMessage("incorrectRemotePassphrase");
            else if ( connection != null) loadTable(connection);
            else updateTitle("Reconnection to Remote Server failed.");
        }
        catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * This method is called from Main JavaFX thread.
     */
    private void manageNewCommunicates(String newMessage) {
        CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
        switch (newMessage) {
            case "cannotConnectToRemoteDb":
                ShortAlertWindowFactory.showWindow(ErrorType.CANNOT_CONNECT_TO_REMOTE_DB);
                startService(); // Wprowadzić enum service flag żeby service wiedział czy po połączeniu ma wyświetlić informacje o tym czy ma przeładować
                currentStatusManager.setErrorStatus("Error (Click here for more info)");
                break;
            case "incorrectRemotePassphrase":
                ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_REMOTE_PASSPHRASE);
                currentStatusManager.setErrorStatus("Error - Incorrect UserName or Passphrase");
                break;
            case "reloadingRemoteServerDatabaseDone":
                currentStatusManager.setProgressValue(0.0);
                currentStatusManager.setInfoStatus("Reloading Remote Server Database Done");
                break;
            default:
                currentStatusManager.setInfoStatus(newMessage);
                break;
        }
    }

    private void loadTable(Connection connection)  {
        CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
        currentStatusManager.setProgressValue(0.05);
        updateMessage("Connecting to Database...");
        final String numberOfRowsQuery = "SELECT COUNT(*) FROM compounds";
        int size = 0;
        try {
            PreparedStatement loadDBStatement = connection.prepareStatement(numberOfRowsQuery);
            ResultSet resultSet = loadDBStatement.executeQuery();
            while (resultSet.next()) size = resultSet.getInt(1); // getting number of rows
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
                currentStatusManager.addToProgressValue(0.9 * ( 1.0 / ((double) size)));
                if (index % 100 == 0) {
                    double loadedPercentage = BigDecimal.valueOf( (double) ++index / ((double) size) * 100 )
                            .setScale(2, RoundingMode.HALF_UP).doubleValue();
                    updateMessage("Loaded " + loadedPercentage + "%");
                }
            }
            updateMessage("Refreshing table");
            observableList.setAll(fullListOfCompounds);
            mainSceneTableView.setItems(observableList);
        }
        catch (SQLException e) { e.printStackTrace(); }
        updateTitle("reloadingRemoteServerDatabaseDone");
    }

    /**
     * This method is called from Main JavaFX thread.
     */
    private void startService() {
        ScheduledService<Void> databasePingService = PingService.getService(mainStageController);
        databasePingService.setPeriod(Duration.seconds(3));
        databasePingService.start();
    }
}














