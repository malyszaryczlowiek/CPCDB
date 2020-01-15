package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ServiceReconnectionHandler implements ConnectionHandler
{
    @Override
    public Connection connect() {
        Connection CONNECTION;
        ConnectionQueryBuilder remoteConnectionQueryBuilder = new RemoteConnectionQueryBuilder();
        remoteConnectionQueryBuilder.addConnectionConfigurations();
        try {
            CONNECTION = DriverManager.getConnection(
                    remoteConnectionQueryBuilder.getQuery(),
                    SecureProperties.getProperty("settings.db.remote.user"),
                    SecureProperties.getProperty("settings.db.remote.passphrase"));
            if ( !SecureProperties.hasProperty("remoteDBExists") ) // if db does not exist, we must create it
                new DatabaseAndTableCreator(CONNECTION, DBNAME, DatabaseLocation.REMOTE);
            ErrorFlagsManager.setErrorFlagTo(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR, false);
            ErrorFlagsManager.setErrorFlagTo(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR, false);
            return CONNECTION;
        }
        catch ( CommunicationsException e) { //CJCommunicationsException |
            ErrorFlagsManager.setErrorFlagTo(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR, true);
            e.printStackTrace();
            return null;
        }
        catch (SQLException e) {
            ErrorFlagsManager.setErrorFlagTo(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR, true);
            e.printStackTrace();
            return null;
        }
    }
}
