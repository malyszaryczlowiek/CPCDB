package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class ServiceReconnectionHandler implements ConnectionHandler
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
            DatabaseAndTableCreator.createIfNotExist(CONNECTION, DatabaseLocation.REMOTE);
            ErrorFlagsManager.resetErrorFlag(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR);
            ErrorFlagsManager.resetErrorFlag(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR);
            return CONNECTION;
        }
        catch ( CommunicationsException | java.sql.SQLNonTransientConnectionException e) { //CJCommunicationsException |
            ErrorFlagsManager.setErrorFlagTo(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR, e.getMessage());
            System.out.println(e.getMessage());
             // TODO tu jak będę implementował loga to nie trzeba robić aby mi wywalał error o braku połączenia co 3s
            return null;
        }
        catch (SQLException e) {
            ErrorFlagsManager.setErrorFlagTo(
                    ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR, e.getMessage());
            System.out.println(e.getMessage());
            return null;
        }
    }
}
