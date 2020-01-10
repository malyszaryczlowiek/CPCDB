package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;
import com.mysql.cj.exceptions.CJCommunicationsException;
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
        catch (CJCommunicationsException | CommunicationsException e) {
            e.printStackTrace();
            System.out.println("jestem w LocalConnectionHandler w pierwszym catchu.");
            return new NoConnectionHandler().connect();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return new NoConnectionHandler().connect();
        }
    }
}
