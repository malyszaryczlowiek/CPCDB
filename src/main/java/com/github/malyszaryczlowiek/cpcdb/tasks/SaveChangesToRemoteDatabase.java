package com.github.malyszaryczlowiek.cpcdb.tasks;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.buffer.BufferExecutor;
import com.github.malyszaryczlowiek.cpcdb.db.ConnectionManager;
import com.github.malyszaryczlowiek.cpcdb.managers.CurrentStatusManager;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ErrorType;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ShortAlertWindowFactory;

import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.SQLException;

public class SaveChangesToRemoteDatabase extends Task<Void>
{
    public static Task<Void> getTask() {
        SaveChangesToRemoteDatabase task = new SaveChangesToRemoteDatabase();
        task.setUpTaskListeners();
        return task;
    }

    private SaveChangesToRemoteDatabase() {}

    private void setUpTaskListeners() {
        messageProperty().addListener( (observable, oldValue, newValue) -> manageNewCommunicates(newValue) );
    }

    @Override
    protected Void call() {
        try (Connection connection = ConnectionManager.reconnectToRemoteDb()) {
            if (ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR))
                updateMessage("cannotConnectToRemoteDB");
            else if (ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR))
                updateMessage("incorrectRemotePassphrase");
            else if (connection != null)  saveChangesToLocalDatabase(connection);
            else updateMessage("otherErrorOccurred");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private void saveChangesToLocalDatabase (Connection connection) {
        BufferExecutor bufferExecutor = BufferExecutor.getBufferExecutor();
        updateMessage("startingSavingData");
        // TODO zaimplemnetować SEJWOWANIE!!!
        updateMessage("dataSavedToRemoteDatabase");
    }

    private void manageNewCommunicates(String newCommunicate) {
        CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
        switch (newCommunicate) {
            case "cannotConnectToRemoteDB":
                ShortAlertWindowFactory.showWindow(ErrorType.CANNOT_CONNECT_TO_REMOTE_DB);
                currentStatusManager.setErrorStatus("Error (click here for more info)", 0.0);
                break;
            case "incorrectRemotePassphrase":
                ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_REMOTE_DB_AUTHORISATION);
                currentStatusManager.setErrorStatus("Error (click here for more info)", 0.0);
                break;
            case "startingSavingData":
                currentStatusManager.setInfoStatus("Saving Data...", 0.0);
                break;
            case "dataSavedToRemoteDatabase":
                currentStatusManager.setInfoStatus("Data saved to Local Database", 0.0);
                break;
            case "otherErrorOccurred":
                ShortAlertWindowFactory.showWindow(ErrorType.UNKNOWN_ERROR_OCCURRED);
                currentStatusManager.setErrorStatus("Error (click here for more info)", 0.0);
                break;
            default:
                break;
        }
    }
}
