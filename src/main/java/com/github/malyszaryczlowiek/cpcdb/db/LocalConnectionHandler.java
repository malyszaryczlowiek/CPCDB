package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;

import java.sql.*;

public class LocalConnectionHandler implements ConnectionHandler
{
    @Override
    public Connection connect() {
        Connection CONNECTION;
        ConnectionQueryBuilder localConnectionQueryBuilder = new LocalConnectionQueryBuilder();
        localConnectionQueryBuilder.addConnectionConfigurations();
        try {
            CONNECTION = DriverManager.getConnection( // tu wywala wyjątek więc trzeba tworzenie nowej bazy zrobić w builderze lub jakoś inaczej. analogia w remote.
                    localConnectionQueryBuilder.getQuery(),
                    SecureProperties.getProperty("settings.db.local.user"),
                    SecureProperties.getProperty("settings.db.local.passphrase"));
            DatabaseAndTableCreator.createIfNotExist(CONNECTION, DatabaseLocation.LOCAL); // TODO Trzeba napisać buildera który nie zawiera nazwy bazy danych bo
            // jeśli wywołamy get connection z nazwą bazy danych a tej bazy nie ma to wywala wyjątek: java.sql.SQLSyntaxErrorException: Unknown database 'Wa1s8JBvyU'
            ErrorFlagsManager.setErrorFlagTo(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR, false);
            ErrorFlagsManager.setErrorFlagTo(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR, false);
            SecureProperties.setProperty("localDBExists", "true");
            return CONNECTION;
        }
        catch ( CommunicationsException e) { // CJCommunicationsException |
            e.printStackTrace();
            ErrorFlagsManager.setErrorFlagTo(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR, true);
            return new NoConnectionHandler().connect();
        }
        catch (SQLException e) {
            e.printStackTrace();
            ErrorFlagsManager.setErrorFlagTo(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR, true);
            return new NoConnectionHandler().connect();
        }
    }
}
// java.io.EOFException: SSL peer shut down incorrectly
// javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake
