package com.github.malyszaryczlowiek.cpcdb.tasks;

import com.github.malyszaryczlowiek.cpcdb.db.ConnectionManager;
import com.github.malyszaryczlowiek.cpcdb.managers.CurrentStatusManager;

import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.SQLException;

public class MergeRemoteDbWithLocal extends Task<Void>
{
    public static Task<Void> getTask() {
        MergeRemoteDbWithLocal task =  new MergeRemoteDbWithLocal();
        task.setUpTaskListeners();
        return task;
    }

    private MergeRemoteDbWithLocal() {}

    private void setUpTaskListeners() {
        this.messageProperty().addListener( (observable, oldMessage, newMessage) -> {
            CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
            currentStatusManager.setInfoStatus(newMessage);
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
                    // TODO AAAAAAAAAAAAAAAAAAAAA dobry programista jest potrzebny od zaraz
                    //TODO to trzeba dokończyć zrobić aby robił merging
                    // TODO nie zapomnieć, że trzeba to
                      zimplementować;
                    ++i;
                }
                updateMessage("workDone");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
