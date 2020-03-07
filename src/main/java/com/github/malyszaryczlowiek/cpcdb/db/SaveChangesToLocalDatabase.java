package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.buffer.BufferExecutor;
import com.github.malyszaryczlowiek.cpcdb.managers.CurrentStatusManager;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ErrorType;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ShortAlertWindowFactory;

import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.SQLException;

public class SaveChangesToLocalDatabase extends Task<Void>
{
    public static Task<Void> getTask() {
        SaveChangesToLocalDatabase task = new SaveChangesToLocalDatabase();
        task.setUpTaskListeners();
        return task;
    }

    private SaveChangesToLocalDatabase() {}

    private void setUpTaskListeners() {
        messageProperty().addListener( (observable, oldValue, newValue) -> manageNewCommunicates(newValue) );
    }

    @Override
    protected Void call() {
        try (Connection connection = ConnectionManager.connectToLocalDb()) {
            if (ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR))
                updateMessage("cannotConnectToLocalDB");
            else if (ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR))
                updateMessage("incorrectLocalPassphrase");
            else if (connection != null)  saveChangesToLocalDatabase(connection);
            else updateMessage("otherErrorOccurred");
            // TODO if (jeśli wątek jest demonem notyfikuj inne wątki o skończeniu działania?)
        }
        catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private void saveChangesToLocalDatabase (Connection connection) {
        BufferExecutor bufferExecutor = BufferExecutor.getBufferExecutor();
        updateMessage("startingSavingData");
        // TODO to trzeba w końcu zaimplementować


        updateMessage("dataSavedToLocalDatabase");
    }

    /**
     * This method is called from Main JavaFX thread.
     * @param newCommunicate Communicate to display in progress bar.
     */
    private void manageNewCommunicates(String newCommunicate) {
        CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
        switch (newCommunicate) {
            case "cannotConnectToLocalDB":
                ShortAlertWindowFactory.showWindow(ErrorType.CANNOT_CONNECT_TO_LOCAL_DB);
                currentStatusManager.setErrorStatus("Error (click here for more info)", 0.0);
                break;
            case "incorrectLocalPassphrase":
                ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_LOCAL_DB_AUTHORISATION);
                currentStatusManager.setErrorStatus("Error (click here for more info)", 0.0);
                break;
            case "startingSavingData":
                currentStatusManager.setInfoStatus("Saving Data...", 0.0);
                break;
            case "dataSavedToLocalDatabase":
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





