package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;

import java.sql.*;

class RemoteConnectionHandler implements ConnectionHandler
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
            return CONNECTION;
        }
        catch ( CommunicationsException e) { //CJCommunicationsException |
            e.printStackTrace();
            ErrorFlagsManager.setErrorFlagTo(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR, true);
            return new LocalConnectionHandler().connect();
        }
        catch (SQLException e) {
            ErrorFlagsManager.setErrorFlagTo(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR, true);
            e.printStackTrace();
            return new LocalConnectionHandler().connect();
        }
    }
    /*
    public Connection connect(Executor executor) {
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
            CONNECTION.setNetworkTimeout(executor, 3000);
            return CONNECTION;
        }
        catch ( CommunicationsException e) { //CJCommunicationsException |
            e.printStackTrace();
            ErrorFlagsManager.setErrorTo(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR, true);
            return new LocalConnectionHandler().connect();
        }
        catch (SQLException e) {
            ErrorFlagsManager.setErrorTo(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR, true);
            e.printStackTrace();
            return new LocalConnectionHandler().connect();
        }
    }
     */
}


/* to jest wiadomość gdy nie ma połączenia z interetem
            Communications link failure


             */
//e.printStackTrace();



/*
jak nie może się połączyć ze zdalnym albo lokalnym serwerem: Communications link failure

The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server.



Inne błędy jakie mogą się pojawić:
Caused by: com.mysql.cj.exceptions.CJCommunicationsException: Communications link failure
Caused by: java.net.UnknownHostException: remotemysql.com: Temporary failure in name resolution

java.sql.SQLException: No timezone mapping entry for 'false'
Caused by: com.mysql.cj.exceptions.WrongArgumentException: No timezone mapping entry for 'false'
 */



























