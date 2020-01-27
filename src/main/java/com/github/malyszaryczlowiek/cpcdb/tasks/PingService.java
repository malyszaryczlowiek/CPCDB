package com.github.malyszaryczlowiek.cpcdb.tasks;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.controllers.MainStageController;
import com.github.malyszaryczlowiek.cpcdb.db.ConnectionManager;
import com.github.malyszaryczlowiek.cpcdb.managers.CurrentStatusManager;
import com.github.malyszaryczlowiek.cpcdb.windows.windowLoaders.WindowFactory;
import com.github.malyszaryczlowiek.cpcdb.windows.windowLoaders.WindowsEnum;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.SQLException;

public class PingService extends ScheduledService<Void>
{
    private static PingService thisService;
    private MainStageController mainStageController;

    public static ScheduledService<Void> getService(MainStageController mainStageController) {
        thisService = new PingService(mainStageController);
        return thisService;
    }

    private PingService(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }

    @Override
    protected Task<Void> createTask() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try (Connection connection = ConnectionManager.reconnectToRemoteDb()) {
                    if (connection != null)
                        updateMessage("connectionEstablished");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        task.messageProperty().addListener( (observable, oldValue, newValue) -> {
            if (newValue.equals("connectionEstablished")) {
                CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
                currentStatusManager.resetFont();
                currentStatusManager.setCurrentStatus("Connection to Remote Database Established");
                thisService.cancel();
                ErrorFlagsManager.setErrorFlagTo(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR, false);
                WindowFactory.showWindow(WindowsEnum.MERGING_REMOTE_DB_WINDOW, mainStageController,null);

                // Updating local database.

            }
        });
        return task;
    }
}
