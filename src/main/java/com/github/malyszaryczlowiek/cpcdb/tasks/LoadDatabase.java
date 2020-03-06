package com.github.malyszaryczlowiek.cpcdb.tasks;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorStatus;
import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.TempStability;
import com.github.malyszaryczlowiek.cpcdb.compound.Unit;
import com.github.malyszaryczlowiek.cpcdb.controllers.MainStageController;
import com.github.malyszaryczlowiek.cpcdb.db.ConnectionManager;
import com.github.malyszaryczlowiek.cpcdb.locks.LockProvider;
import com.github.malyszaryczlowiek.cpcdb.locks.LockTypes;
import com.github.malyszaryczlowiek.cpcdb.managers.CurrentStatusManager;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ErrorType;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ShortAlertWindowFactory;

import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class LoadDatabase extends Task<String>
{
    private LockProvider lockProvider;
    private DoubleProperty progressValue;
    private List<Compound> fullListOfCompounds;
    private ObservableList<Compound> observableList;
    private TableView<Compound> mainSceneTableView;
    private CurrentStatusManager currentStatusManager;
    private MainStageController mainStageController;

    private LoadDatabase(DoubleProperty progressValue, List<Compound> fullListOfCompounds,
                         ObservableList<Compound> observableList, TableView<Compound> mainSceneTableView,
                         CurrentStatusManager currentStatusManager, MainStageController mainStageController) {
        lockProvider = LockProvider.getLockProvider();
        this.progressValue = progressValue;
        this.fullListOfCompounds = fullListOfCompounds;
        this.observableList = observableList;
        this.mainSceneTableView = mainSceneTableView;
        this.currentStatusManager = currentStatusManager;
        this.mainStageController = mainStageController;
    }

    public static Task<String> getTask(DoubleProperty progressValue, List<Compound> fullListOfCompounds,
                                       ObservableList<Compound> observableList, TableView<Compound> mainSceneTableView,
                                       CurrentStatusManager currentStatusManager, MainStageController mainStageController) {
        LoadDatabase thisTask = new LoadDatabase(progressValue, fullListOfCompounds, observableList,
                mainSceneTableView, currentStatusManager, mainStageController);
        thisTask.setUpTaskListeners();
        return thisTask;
    }

    /**
     * This method is called only from the main JavaFX Application Thread.
     * All bodies of listeners are called from JavaFx Application Thread as well.
     */
    private void setUpTaskListeners() {
        this.messageProperty().addListener( (observableValue, oldString, newString) -> manageNewCommunicates(newString) );
        this.valueProperty().addListener( (observable, oldValue, newValue) -> manageNewCommunicates(newValue) );
        this.titleProperty().addListener( (observable, oldValue, newValue) -> {
            if ("updateLocalDb".equals(newValue)) {
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
                updateValue("cannotConnectToRemoteDB");
                loadTable(connection); }// working on local copy
            else if ( incorrectRemotePassphrase && connectionToLocalDb )
                updateValue("incorrectRemotePassphraseAndCannotConnectToLocalDatabase");// not working on any copy
            else if ( incorrectRemotePassphrase && incorrectLocalPassphrase )
                updateValue("incorrectRemoteAndLocalPassphrase");// not working on any copy
            else if ( !incorrectLocalPassphrase && !connectionToLocalDb && incorrectRemotePassphrase && connection != null) {
                updateValue("incorrectRemotePassphrase");
                loadTable(connection); }// working on local copy
            else if (connection != null && noErrors) loadTable(connection); // all working ok
            else updateMessage("UnknownErrorOccurred");
        }
        catch (SQLException e) { e.printStackTrace(); }
        return "taskEnded";
    }

    private void loadTable(Connection connection)  {
        updateMessage("Connecting to Database...");
        final String numberOfRowsQuery = "SELECT COUNT(*) FROM compounds";
        int size = 0;
        try {
            PreparedStatement loadDBStatement = connection.prepareStatement(numberOfRowsQuery);
            ResultSet resultSet = loadDBStatement.executeQuery();
            while (resultSet.next())
                size = resultSet.getInt(1); // getting number of rows
        } catch (SQLException e) { e.printStackTrace(); }
        final String loadDBSQLQuery = "SELECT * FROM compounds";
        try {
            PreparedStatement loadDBStatement = connection.prepareStatement(loadDBSQLQuery);
            synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                progressValue.setValue( progressValue.get() + 0.1);
            }
            updateMessage("Downloading data from Server");
            ResultSet resultSet = loadDBStatement.executeQuery();
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
                double loadedPercentage = Math.round( (double) ++index / ((double) size) * 100);
                synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                    progressValue.setValue( progressValue.get() + 0.6 * ( 1.0 / ((double) size)));
                }
                if (index % 100 == 0) updateMessage("Loaded " + loadedPercentage + "%");
            }
            updateMessage("Refreshing table");
            observableList.setAll(fullListOfCompounds);
            mainSceneTableView.setItems(observableList);
        }
        catch (SQLException e) { e.printStackTrace(); }
        synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) { progressValue.setValue(0.0); }
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
    }

    /**
     * This method is called from Main JavaFX thread.
     */
    private void manageNewCommunicates(String newString) {
        switch (newString) {
            case "cannotConnectToAllDB":
                ShortAlertWindowFactory.showWindow(ErrorType.CANNOT_CONNECT_TO_ALL_DB);
                startService();
                currentStatusManager.setErrorStatus("Error (click here for more info)", 0.0);
                break;
            case "cannotConnectToRemoteDatabaseAndIncorrectLocalUsernameOrPassphrase":
                ShortAlertWindowFactory.showWindow(ErrorType.CANNOT_CONNECT_TO_REMOTE_BD_AND_INCORRECT_LOCAL_PASSPHRASE);
                startService();
                currentStatusManager.setErrorStatus("Error (click here for more info)", 0.0);
                break;
            case "cannotConnectToRemoteDB":
                ShortAlertWindowFactory.showWindow(ErrorType.CANNOT_CONNECT_TO_REMOTE_DB);
                startService();
                currentStatusManager.setWarningMessage("Warning (click here for more info)");
                break;
            case "incorrectRemotePassphraseAndCannotConnectToLocalDatabase":
                ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_REMOTE_PASSPHRASE_AND_CANNOT_CONNECT_TO_LOCAL_DB);
                currentStatusManager.setErrorStatus("Error (click here for more info)", 0.0);
                break;
            case "incorrectRemoteAndLocalPassphrase":
                ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_REMOTE_AND_LOCAL_PASSPHRASE);
                currentStatusManager.setErrorStatus("Error (click here for more info)", 0.0);
                break;
            case "incorrectRemotePassphrase":
                ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_REMOTE_PASSPHRASE);
                currentStatusManager.setWarningMessage("Warning (click here for more info)");
                break;
            case "showWarning":
                currentStatusManager.setWarningMessage("Warning(click here for more info)");
                currentStatusManager.setProgressValue(0.0);
                break;
            case "UnknownErrorOccurred":
                ShortAlertWindowFactory.showWindow(ErrorType.UNKNOWN_ERROR_OCCURRED);
                currentStatusManager.setErrorStatus("Unknown Error Occurred", 0.0);
                break;
            case "taskEnded":// do nothing
                break;
            default:  // correct data loading
                currentStatusManager.setInfoStatus(newString, progressValue.doubleValue());
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







