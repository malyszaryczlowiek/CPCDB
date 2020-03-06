package com.github.malyszaryczlowiek.cpcdb.tasks;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.TempStability;
import com.github.malyszaryczlowiek.cpcdb.compound.Unit;
import com.github.malyszaryczlowiek.cpcdb.db.ConnectionManager;
import com.github.malyszaryczlowiek.cpcdb.locks.LockProvider;
import com.github.malyszaryczlowiek.cpcdb.locks.LockTypes;

import com.github.malyszaryczlowiek.cpcdb.managers.CurrentStatusManager;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class LoadDatabaseFromRemoteWithoutMerging extends Task<Void>
{
    private LockProvider lockProvider;
    private DoubleProperty progressValue;
    private List<Compound> fullListOfCompounds;
    private ObservableList<Compound> observableList;
    private TableView<Compound> mainSceneTableView;

    public static Task<Void> getTask(DoubleProperty progressValue, List<Compound> fullListOfCompounds,
                                     ObservableList<Compound> observableList, TableView<Compound> mainSceneTableView) {
        LoadDatabaseFromRemoteWithoutMerging task = new LoadDatabaseFromRemoteWithoutMerging(
                        progressValue, fullListOfCompounds, observableList, mainSceneTableView);
        task.setUpTaskListeners();
        return task;
    }

    private LoadDatabaseFromRemoteWithoutMerging(DoubleProperty progressValue, List<Compound> fullListOfCompounds,
                                   ObservableList<Compound> observableList, TableView<Compound> mainSceneTableView) {
        lockProvider = LockProvider.getLockProvider();
        this.progressValue = progressValue;
        this.fullListOfCompounds = fullListOfCompounds;
        this.observableList = observableList;
        this.mainSceneTableView = mainSceneTableView;
    }

    private void setUpTaskListeners() {
        this.messageProperty().addListener( (observable, oldMessage, newMessage) -> {
                    CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
                    currentStatusManager.setCurrentStatus(newMessage);
        });
        this.progressProperty().addListener((observable, oldValue, newValue) -> {
                    CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
                    currentStatusManager.setProgressValue( (Double) newValue );
        });
        this.titleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("Reloading Remote Server Database Done")){
                CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
                currentStatusManager.setCurrentStatus("Reloading Remote Server Database Done");
                zrobić sprawdzenie z tryToLoadToLocalDb // if (SecureProperties.getProperty("tryToLoadToLocalDb").equals("true))
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
            boolean connectedToRemoteDb =ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR);
            if ( !connectedToRemoteDb && connection != null) loadTable(connection);
            else updateMessage("Reconnection to Remote Server failed.");
        }
        catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private void loadTable(Connection connection)  {
        updateMessage("Connecting to Database");
        updateProgress(0.0,1.0);
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
            synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                progressValue.setValue( progressValue.get() + 0.1);
            }
            updateMessage("Downloading data from Remote Server");
            updateProgress(0.05, 1.0);
            ResultSet resultSet = loadDBStatement.executeQuery();
            updateMessage("Loading downloaded data");
            updateProgress(0.1, 1.0);
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
                ++index;
                if (index % 100 == 0) {
                    double loadedPercentage = Math.round( (double) index / ((double) size) * 100);
                    updateMessage("Loaded " + loadedPercentage + "%");
                    updateProgress(0.1 + loadedPercentage * 0.9, 1.0);
                }
            }
            updateMessage("Refreshing Table");
            observableList.setAll(fullListOfCompounds);
            mainSceneTableView.setItems(observableList);
        }
        catch (SQLException e) { e.printStackTrace(); }
        updateProgress(0.0,1.0);
        updateTitle("Reloading Remote Server Database Done");
    }
}














