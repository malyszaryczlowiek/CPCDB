package com.github.malyszaryczlowiek.cpcdb.tasks;

import com.github.malyszaryczlowiek.cpcdb.db.ConnectionManager;
import com.github.malyszaryczlowiek.cpcdb.locks.LockProvider;
import com.github.malyszaryczlowiek.cpcdb.locks.LockTypes;

import com.github.malyszaryczlowiek.cpcdb.managers.CurrentStatusManager;
import javafx.beans.property.DoubleProperty;
import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.SQLException;



public class MergeRemoteDbWithLocal extends Task<Void>
{
    private DoubleProperty progressValue;
    private LockProvider lockProvider;

    public static Task<Void> getTask(DoubleProperty progressValue) {
        MergeRemoteDbWithLocal task =  new MergeRemoteDbWithLocal(progressValue);
        task.setUpTaskListeners();
        return task;
    }

    private MergeRemoteDbWithLocal(DoubleProperty progressValue) {
        this.progressValue = progressValue;
        lockProvider = LockProvider.getLockProvider();
    }

    private void setUpTaskListeners() {
        this.messageProperty().addListener( (observable, oldMessage, newMessage) -> {
            CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
            currentStatusManager.setCurrentStatus(newMessage);
        });
        this.progressProperty().addListener((observable, oldValue, newValue) -> {
            CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
            currentStatusManager.setProgressValue((Double) newValue);
        });
    }

    @Override
    protected Void call() {
        try (Connection connection = ConnectionManager.reconnectToRemoteDb()) {
            if (connection != null) {
                int i = 0;
                updateMessage("Loading");
                while (i < 100) {
                    synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                        progressValue.set(i * 0.01);
                    }
                    updateProgress(i * 0.01, 1.0);
                    //TODO to trzeba dokończyć zrobić aby robił merging
                    ++i;
                }
                updateMessage("workDone");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
