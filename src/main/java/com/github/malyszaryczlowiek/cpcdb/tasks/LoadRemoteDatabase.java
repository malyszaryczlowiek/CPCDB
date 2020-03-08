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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * W tej klasie:
 * 1. pobieramy dane ze zdalnego servera, (NIE wchodzimy w pobieranie z lokalnego)
 * 2. wczytujemy je do programu, (lub wyświetlamy komunikat o błędzie)
 * 3. wyświetlamy w tabeli
 * 4. zapisujemy na lokalnym serwerze (jeśli trzeba) === UpdateLocalDb
 */
public class LoadRemoteDatabase extends Task<String>
{
    private List<Compound> fullListOfCompounds;
    private ObservableList<Compound> observableList;
    private TableView<Compound> mainSceneTableView;
    private MainStageController mainStageController;
    private ScheduledService<Void> databasePingService;
    private boolean displayInTable;
    private OperationFlag operationFlag;

    /**
     * This method is called from Main JavaFX thread.
     */
    public static Task<String> getTask( List<Compound> fullListOfCompounds,
                                        ObservableList<Compound> observableList, TableView<Compound> mainSceneTableView,
                                        MainStageController mainStageController, ScheduledService<Void> databasePingService,
                                        boolean displayInTable, OperationFlag operationFlag) {
        LoadRemoteDatabase task = new LoadRemoteDatabase( fullListOfCompounds, observableList, mainSceneTableView,
                mainStageController, databasePingService, displayInTable, operationFlag);
        task.setUpTaskListeners();
        return task;
    }

    /**
     * This method is called from Main JavaFX thread.
     */
    private LoadRemoteDatabase(List<Compound> fullListOfCompounds,
                               ObservableList<Compound> observableList, TableView<Compound> mainSceneTableView,
                               MainStageController mainStageController, ScheduledService<Void> databasePingService,
                               boolean displayInTable, OperationFlag operationFlag) {
        this.fullListOfCompounds = fullListOfCompounds;
        this.observableList = observableList;
        this.mainSceneTableView = mainSceneTableView;
        this.mainStageController = mainStageController;
        this.databasePingService = databasePingService;
        this.displayInTable = displayInTable;
        this.operationFlag = operationFlag;
    }

    /**
     * This method is called from Main JavaFX thread.
     */
    private void setUpTaskListeners() {
        messageProperty().addListener( (observable, oldMessage, newMessage) -> manageNewCommunicates(newMessage) );
        titleProperty().addListener((observable, oldValue, newValue) -> manageNewCommunicates(newValue));
    }

    /**
     * This method is called from Main JavaFX thread.
     */
    private void manageNewCommunicates(String newMessage) {
        CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
        switch (newMessage) {
            case "cannotConnectToRemoteDb":
                ShortAlertWindowFactory.showWindow(ErrorType.CANNOT_CONNECT_TO_REMOTE_DB);
                if ( SecureProperties.getProperty("tryToConnectToLocalDb").equals("true") ) {
                    if (operationFlag.equals(OperationFlag.SAVING)) saveToLocalDatabase();
                    else loadFromLocalDatabase(); }
                else currentStatusManager.setErrorStatus("Error (Click here for more info)");
                if (databasePingService == null) startService();
                else if ( !databasePingService.isRunning() ) databasePingService.restart();
                break;
            case "incorrectRemotePassphrase":
                ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_REMOTE_PASSPHRASE);
                if ( SecureProperties.getProperty("tryToConnectToLocalDb").equals("true") ) {
                    if (operationFlag.equals(OperationFlag.SAVING)) saveToLocalDatabase();
                    else loadFromLocalDatabase(); }
                else currentStatusManager.setErrorStatus("Error - Incorrect UserName or Passphrase");
                break;
            case "reloadingRemoteServerDatabaseDone":
                currentStatusManager.setProgressValue(0.0);
                if ( SecureProperties.getProperty("tryToConnectToLocalDb").equals("true") )
                    saveToLocalDatabase();
                else currentStatusManager.setInfoStatus("Reloading Remote Server Database Done");
                break;
            case "unknownError":
                ShortAlertWindowFactory.showWindow(ErrorType.UNKNOWN_ERROR_OCCURRED);
                currentStatusManager.setErrorStatus("Unknown Error Occurred");
                break;
            default:
                currentStatusManager.setInfoStatus(newMessage);
                break;
        }
    }

    private void saveToLocalDatabase( ) {
        Task<String> updateLocalDatabaseTask = UpdateLocalDatabase.getTask(observableList);
        Thread updateLocalDatabaseThread = new Thread(updateLocalDatabaseTask);
        updateLocalDatabaseThread.setDaemon(displayInTable);
        updateLocalDatabaseThread.start();
        try { updateLocalDatabaseThread.join(); }
        catch (InterruptedException e) { e.printStackTrace(); }
    }

    private void loadFromLocalDatabase() {
        Task<String> loadLocalDatabaseTask = LoadLocalDatabase.getTask(fullListOfCompounds,observableList,mainSceneTableView);
        Thread loadLocalDatabaseThread = new Thread(loadLocalDatabaseTask);
        loadLocalDatabaseThread.setDaemon(true);
        loadLocalDatabaseThread.start();
    }

    @Override
    protected String call() {
        try (Connection connection = ConnectionManager.connectToRemoteDb()) {
            boolean connectedToRemoteDb = ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR);
            boolean incorrectRemotePassphrase = ErrorFlagsManager.getError(
                    ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR);
            if (connectedToRemoteDb) updateMessage("cannotConnectToRemoteDb");
            else if (incorrectRemotePassphrase) updateMessage("incorrectRemotePassphrase");
            else if ( connection != null) loadTable(connection);
            else updateMessage("unknownError");
        }
        catch (SQLException e) { e.printStackTrace(); }
        return "Task ended";
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
                if (index % 100 == 0) updateMessage("Loaded "+Math.round((double) ++index/((double) size)*100 )+"%");
            }
            updateMessage("Refreshing table");
            if (displayInTable) observableList.setAll(fullListOfCompounds);
            if (displayInTable) mainSceneTableView.setItems(observableList);
        }
        catch (SQLException e) { e.printStackTrace(); }
        updateTitle("reloadingRemoteServerDatabaseDone");
    }

    /**
     * This method is called from Main JavaFX thread.
     */
    private void startService() {
        databasePingService = PingService.getService(mainStageController);
        databasePingService.setPeriod(Duration.seconds(3));
        databasePingService.start();
    }
}














