package com.github.malyszaryczlowiek.cpcdb.tasks;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
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
import java.sql.SQLException;
import java.util.List;

public class UpdateRemoteDatabase extends Task<String>
{
    private List<Compound> fullListOfCompounds;
    private ObservableList<Compound> observableList;
    private TableView<Compound> mainSceneTableView;
    private MainStageController mainStageController;
    private ScheduledService<Void> databasePingService;
    private boolean displayInTable;

    public static Task<String> getTask(List<Compound> fullListOfCompounds, ObservableList<Compound> observableList,
                                       TableView<Compound> mainSceneTableView, MainStageController mainStageController,
                                       ScheduledService<Void> databasePingService, boolean displayInTable) {
        UpdateRemoteDatabase updateLocalDatabase = new UpdateRemoteDatabase(fullListOfCompounds, observableList,
                mainSceneTableView, mainStageController, databasePingService, displayInTable);
        updateLocalDatabase.setUpTaskListeners();
        return updateLocalDatabase;
    }

    private UpdateRemoteDatabase(List<Compound> fullListOfCompounds,
                                 ObservableList<Compound> observableList, TableView<Compound> mainSceneTableView,
                                 MainStageController mainStageController, ScheduledService<Void> databasePingService,
                                 boolean displayInTable) {
        this.fullListOfCompounds = fullListOfCompounds;
        this.observableList = observableList;
        this.mainSceneTableView = mainSceneTableView;
        this.mainStageController = mainStageController;
        this.displayInTable = displayInTable;
        this.databasePingService = databasePingService;
    }

    /**
     * This method is called only from the main JavaFX Application Thread.
     * All bodies of listeners are called from JavaFx Application Thread as well.
     */
    private void setUpTaskListeners() {
        titleProperty().addListener((observable, oldValue, newValue) -> manageNewMessages(newValue) );
        messageProperty().addListener((observable2, oldValue2, newValue2) -> manageNewMessages(newValue2));
    }

    private void manageNewMessages(String message) {
        CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
        switch (message) {
            case "connectionError":
                ShortAlertWindowFactory.showWindow(ErrorType.CANNOT_CONNECT_TO_REMOTE_DB);
                if ( SecureProperties.getProperty("tryToConnectToLocalDb").equals("true") )
                    saveToLocalDatabase();
                else currentStatusManager.setErrorStatus("Error, Cannot Connect to Remote Database");
                if (databasePingService == null) startService();
                else if ( !databasePingService.isRunning() ) databasePingService.restart();
                break;
            case "incorrectPassphrase":
                ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_REMOTE_DB_AUTHORISATION);
                if ( SecureProperties.getProperty("tryToConnectToLocalDb").equals("true") )
                    saveToLocalDatabase();
                else currentStatusManager.setErrorStatus("Error, Incorrect Remote Database Authorisation");
                break;
            case "updatingLocalDatabase":
                currentStatusManager.setInfoStatus("Updating Remote Database...");
                break;
            case "remoteDbUpdated":
                currentStatusManager.setProgressValue(0.0);
                currentStatusManager.setInfoStatus("Remote Database Updated"); // start loading from remote to program
                Task<String> loadingDatabaseTask = LoadRemoteDatabase.getTask(fullListOfCompounds, observableList,
                        mainSceneTableView, mainStageController, databasePingService, displayInTable, OperationFlag.SAVING);
                Thread loadingDatabaseThread = new Thread(loadingDatabaseTask);
                loadingDatabaseThread.setDaemon(displayInTable);
                loadingDatabaseThread.start();
                if (displayInTable) {
                    try { loadingDatabaseThread.join(); }
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
                break;
            case "unknownError":
                ShortAlertWindowFactory.showWindow(ErrorType.UNKNOWN_ERROR_OCCURRED);
                currentStatusManager.setErrorStatus("Unknown Error");
                break;
            default:
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

    @Override
    protected String call() {
        try ( Connection connection = ConnectionManager.connectToRemoteDb() ) {
            boolean connectionToLocalDb = ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR);
            boolean incorrectLocalPassphrase = ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR);
            if ( connectionToLocalDb && connection == null ) updateMessage("connectionError");
            else if ( incorrectLocalPassphrase && connection == null ) updateMessage("incorrectPassphrase");
            else if ( !incorrectLocalPassphrase && !connectionToLocalDb && connection != null )
                updateRemoteDatabase(connection);
            else updateMessage("unknownError");
        }
        catch (SQLException e) { e.printStackTrace(); }
        return "taskEnded";
    }

    private void updateRemoteDatabase(Connection connection) {

        if (displayInTable) // TODO to impkr=ement
        updateTitle("remoteDbUpdated");
    }

    private void startService() {
        databasePingService = PingService.getService(mainStageController);
        databasePingService.setPeriod(Duration.seconds(3));
        databasePingService.start();
    }
}




















/*

        final String clearTableQuery = "DELETE FROM compounds";
        try {
            connection.setAutoCommit(false);
            PreparedStatement clearTableStatement = connection.prepareStatement(clearTableQuery);
            clearTableStatement.executeUpdate();
            int index = 0;
            final int size = observableList.size();
            CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
            for (Compound compound: observableList) {
                Integer compoundId = compound.getId();
                String smiles = compound.getSmiles();
                String compoundNumber = compound.getCompoundNumber();
                float amount = compound.getAmount();
                String unit = compound.getUnit().toString();
                String form = compound.getForm();
                String stability = compound.getTempStability().toString();
                String container = compound.getContainer();
                boolean argon = compound.isArgon();
                String storagePlace = compound.getStoragePlace();
                LocalDateTime modificationDate = compound.getDateTimeModification();
                String additionalInformation = compound.getAdditionalInfo();

                String insertQuery = "INSERT INTO compounds(CompoundID, Smiles, CompoundNumber, Amount, Unit, " +
                        "Form, Stability, Argon, Container, " +
                        "StoragePlace, LastModification, AdditionalInfo) " +
                        "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement addingStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                addingStatement.setInt(1, compoundId);
                addingStatement.setString(2, smiles);
                addingStatement.setString(3, compoundNumber);
                addingStatement.setFloat(4, amount);
                addingStatement.setString(5, unit);
                addingStatement.setString(6, form);
                addingStatement.setString(7, stability);
                addingStatement.setBoolean(8, argon);
                addingStatement.setString(9, container);
                addingStatement.setString(10, storagePlace);
                addingStatement.setTimestamp(11, Timestamp.valueOf(modificationDate));
                addingStatement.setString(12, additionalInformation);
                addingStatement.executeUpdate();

                double loadedPercentage = Math.round( (double) ++index / ((double) size) * 100);
                currentStatusManager.addToProgressValue(  0.9 * ( 1.0 / ((double) size)) ); // 0.9 * ( 1.0 / ((double) size))
                if (index % 100 == 0)  updateMessage("Loaded " + loadedPercentage + "%");
                // można jeszcze spróbować rozwiązania z
                // https://www.mysqltutorial.org/mysql-jdbc-transaction/
                // gdzie ustawia się na początku po stroworzeniu connection setAutoCommit(false)
                // i wtedy grupujemy wszystkie polecenia do wykonania i robimy jeden commit()
                // na końcu.
            }
            connection.commit();
        } catch (SQLException e) { e.printStackTrace(); }
 */





