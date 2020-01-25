package com.github.malyszaryczlowiek.cpcdb.tasks;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.db.ConnectionManager;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.SQLException;

public class UpdateLocalDb extends Task<Void>
{
    private ObservableList<Compound> observableList;

    public static Task<Void> getTask(ObservableList<Compound> observableList) {
        return new UpdateLocalDb(observableList);
    }

    private UpdateLocalDb(ObservableList<Compound> observableList) {
        this.observableList = observableList;
    }

    @Override
    protected Void call() {
        try ( Connection connection = ConnectionManager.connectToLocalDb() ) {
            if ( ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR)
                    && connection == null )
                updateMessage("connectionError");
            else if ( ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR)
                    && connection == null )
                updateMessage("incorrectPassphrase");
            else if ( !ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR)
                    && !ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR)
                    && connection != null ) {
                updateMessage("updatingLocalDatabase");
                updateLocalDatabase(connection);
            }
            else
                updateMessage("UnknownError.");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateLocalDatabase(Connection connection) {

    }
}
