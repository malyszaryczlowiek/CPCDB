package com.github.malyszaryczlowiek.cpcdb.tasks;

import com.github.malyszaryczlowiek.cpcdb.db.ConnectionManager;
import com.github.malyszaryczlowiek.cpcdb.locks.LockProvider;
import com.github.malyszaryczlowiek.cpcdb.locks.LockTypes;
import javafx.beans.property.DoubleProperty;
import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.SQLException;

public class MergingWithRemote extends Task<Void>
{
    private DoubleProperty progressValue;
    private LockProvider lockProvider;

    public static Task<Void> getTask(DoubleProperty progressValue) {
        return new MergingWithRemote(progressValue);
    }

    private MergingWithRemote(DoubleProperty progressValue) {
        this.progressValue = progressValue;
        lockProvider = LockProvider.getLockProvider();
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
                    stopThisThread(30);
                    ++i;
                }
                updateMessage("workDone");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void stopThisThread(int milliseconds) {
        try {
            synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                lockProvider.getLock(LockTypes.PROGRESS_VALUE).wait(milliseconds);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
