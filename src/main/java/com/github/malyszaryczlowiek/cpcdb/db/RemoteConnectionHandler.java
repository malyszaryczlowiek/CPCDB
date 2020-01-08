package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.controllers.MainStageController;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;
import com.mysql.cj.exceptions.CJCommunicationsException;
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
            if ( !SecureProperties.hasProperty("remoteDBExists") ) // if db does not exist, we must create it
                new DatabaseAndTableCreator(CONNECTION, DBNAME, DatabaseLocation.REMOTE);
            return CONNECTION;
        }
        catch (CJCommunicationsException | CommunicationsException e) {
            e.printStackTrace();
            MainStageController.setErrorConnectionToRemoteDBToTrue();
            return new LocalConnectionHandler().connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

/*
jak nie może się połączyć ze zdalnym serwerem: Communications link failure

Caused by: com.mysql.cj.exceptions.CJCommunicationsException: Communications link failure

Caused by: java.net.UnknownHostException: remotemysql.com: Temporary failure in name resolution

java.sql.SQLException: No timezone mapping entry for 'false'

Caused by: com.mysql.cj.exceptions.WrongArgumentException: No timezone mapping entry for 'false'
 */



























