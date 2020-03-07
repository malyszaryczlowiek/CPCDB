package com.github.malyszaryczlowiek.cpcdb.tasks;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorStatus;
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

public class LoadDatabase extends Task<String>
{
    private List<Compound> fullListOfCompounds;
    private ObservableList<Compound> observableList;
    private TableView<Compound> mainSceneTableView;
    private CurrentStatusManager currentStatusManager;
    private MainStageController mainStageController;

    private LoadDatabase( List<Compound> fullListOfCompounds, ObservableList<Compound> observableList,
                          TableView<Compound> mainSceneTableView, CurrentStatusManager currentStatusManager,
                          MainStageController mainStageController) {
        this.fullListOfCompounds = fullListOfCompounds;
        this.observableList = observableList;
        this.mainSceneTableView = mainSceneTableView;
        this.currentStatusManager = currentStatusManager;
        this.mainStageController = mainStageController;
    }

    public static Task<String> getTask( List<Compound> fullListOfCompounds, ObservableList<Compound> observableList,
                                        TableView<Compound> mainSceneTableView, CurrentStatusManager currentStatusManager,
                                        MainStageController mainStageController) {
        LoadDatabase thisTask = new LoadDatabase( fullListOfCompounds, observableList,
                mainSceneTableView, currentStatusManager, mainStageController);
        thisTask.setUpTaskListeners();
        return thisTask;
    }

    /**
     * This method is called only from the main JavaFX Application Thread.
     * All bodies of listeners are called from JavaFx Application Thread as well.
     */
    private void setUpTaskListeners() {
        messageProperty().addListener( (observableValue, oldString, newString) -> manageNewCommunicates(newString) );
        valueProperty().addListener( (observable, oldValue, newValue) -> manageNewCommunicates(newValue) );
        titleProperty().addListener( (observable, oldValue, newValue) -> {
            CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
            currentStatusManager.setProgressValue(0.0);
            currentStatusManager.setInfoStatus("Data loaded, table refreshed");
            if (SecureProperties.getProperty("tryToConnectToLocalDb").equals("true") && "updateLocalDb".equals(newValue)) {
                Task<Void> updateLocalDbTask = UpdateLocalDb.getTask(observableList);
                Thread updateLocalDbThread = new Thread(updateLocalDbTask);
                updateLocalDbThread.setDaemon(true);
                updateLocalDbThread.start();
            }
        });
    }

    @Override
    protected String call() {
        try (Connection connection = ConnectionManager.connectToAnyDb()) {
            boolean connectionToRemoteDb = ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR);
            boolean connectionToLocalDb = ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR);
            boolean incorrectRemotePassphrase = ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR);
            boolean incorrectLocalPassphrase = ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR);
            boolean noErrors = ErrorFlagsManager.getMapOfErrors().values().stream().noneMatch( ErrorStatus::getErrorOccurred );
            if ( connectionToRemoteDb && connectionToLocalDb )
                updateValue("cannotConnectToAllDB"); // not working on any copy
            else if ( connectionToRemoteDb && incorrectLocalPassphrase )
                updateValue("cannotConnectToRemoteDatabaseAndIncorrectLocalUsernameOrPassphrase"); // not working on any copy
            else if ( !incorrectLocalPassphrase && !connectionToLocalDb && connectionToRemoteDb && connection != null ) {
                loadTable(connection);
                updateValue("cannotConnectToRemoteDB");}// working on local copy
            else if ( incorrectRemotePassphrase && connectionToLocalDb )
                updateValue("incorrectRemotePassphraseAndCannotConnectToLocalDatabase");// not working on any copy
            else if ( incorrectRemotePassphrase && incorrectLocalPassphrase )
                updateValue("incorrectRemoteAndLocalPassphrase");// not working on any copy
            else if ( !incorrectLocalPassphrase && !connectionToLocalDb && incorrectRemotePassphrase && connection != null) {
                loadTable(connection);
                updateValue("incorrectRemotePassphrase"); }// working on local copy
            else if (connection != null && noErrors) {
                updateValue("Connecting to Database...");
                loadTable(connection); }// all working ok
            else updateValue("UnknownErrorOccurred");
        }
        catch (SQLException e) { e.printStackTrace(); }
        return "taskEnded";
    }

    private void loadTable(Connection connection)  {
        CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
        currentStatusManager.setProgressValue(0.05);
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
            updateMessage("Downloading data from Server");
            ResultSet resultSet = loadDBStatement.executeQuery();
            currentStatusManager.setProgressValue(0.1);
            updateMessage("Loading downloaded data");
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
        updateTitle("updateLocalDb");
    }

    /**
     * This method is called from Main JavaFX thread.
     */
    private void manageNewCommunicates(String newString) {
        switch (newString) {
            case "cannotConnectToAllDB":
                ShortAlertWindowFactory.showWindow(ErrorType.CANNOT_CONNECT_TO_ALL_DB);
                startService();
                currentStatusManager.setErrorStatus("Error (click here for more info)");
                break;
            case "cannotConnectToRemoteDatabaseAndIncorrectLocalUsernameOrPassphrase":
                ShortAlertWindowFactory.showWindow(ErrorType.CANNOT_CONNECT_TO_REMOTE_BD_AND_INCORRECT_LOCAL_PASSPHRASE);
                startService();
                currentStatusManager.setErrorStatus("Error (click here for more info)");
                break;
            case "cannotConnectToRemoteDB":
                ShortAlertWindowFactory.showWindow(ErrorType.CANNOT_CONNECT_TO_REMOTE_DB);
                startService();
                currentStatusManager.setWarningStatus("Warning (click here for more info)");
                break;
            case "incorrectRemotePassphraseAndCannotConnectToLocalDatabase":
                ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_REMOTE_PASSPHRASE_AND_CANNOT_CONNECT_TO_LOCAL_DB);
                currentStatusManager.setErrorStatus("Error (click here for more info)");
                break;
            case "incorrectRemoteAndLocalPassphrase":
                ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_REMOTE_AND_LOCAL_PASSPHRASE);
                currentStatusManager.setErrorStatus("Error (click here for more info)");
                break;
            case "incorrectRemotePassphrase":
                ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_REMOTE_PASSPHRASE);
                currentStatusManager.setWarningStatus("Warning (click here for more info)");
                break;
            case "UnknownErrorOccurred":
                ShortAlertWindowFactory.showWindow(ErrorType.UNKNOWN_ERROR_OCCURRED);
                currentStatusManager.setErrorStatus("Unknown Error Occurred");
                break;
            case "taskEnded":// do nothing, all done
                break;
            default:  // correct data loading
                currentStatusManager.setInfoStatus(newString);
                break;
        }
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

/*
       boolean showWarning = ( ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR)
                && !ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR)
                && !ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR) )
                ||
                ( ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR)
                        && !ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR)
                        && !ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR) );
        if ( showWarning ) updateMessage("showWarning");
        else {
            updateMessage("Data loaded, table refreshed");
            updateTitle("updateLocalDb");
        }
 */





