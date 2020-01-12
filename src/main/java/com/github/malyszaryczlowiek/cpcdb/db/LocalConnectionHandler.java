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
            CONNECTION = DriverManager.getConnection(
                    localConnectionQueryBuilder.getQuery(),
                    SecureProperties.getProperty("settings.db.local.user"),
                    SecureProperties.getProperty("settings.db.local.passphrase"));
            if ( !SecureProperties.hasProperty("localDBExists") ) // if db does not exist, we must create it
                new DatabaseAndTableCreator(CONNECTION, DBNAME, DatabaseLocation.LOCAL);
            return CONNECTION;
        }
        catch ( CommunicationsException e) { // CJCommunicationsException |
            e.printStackTrace();
            // System.out.println("jestem w LocalConnectionHandler w pierwszym catchu.");
            // System.out.println("Message:\n" + e.getMessage());
            ErrorFlagsManager.setErrorTo(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR, true);
            return new NoConnectionHandler().connect();
        }
        catch (SQLException e) {
            e.printStackTrace();
            //String message = e.getMessage(); // Access denied for user 'Wa1s8JBvy'@'89.64.23.97' (using password: YES)
            //if (message.contains("Access denied for user"))
                ErrorFlagsManager.setErrorTo(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR, true);
            return new NoConnectionHandler().connect();
        }
    }
}
