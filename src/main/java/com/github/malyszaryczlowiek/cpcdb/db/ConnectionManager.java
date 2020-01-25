package com.github.malyszaryczlowiek.cpcdb.db;

import java.sql.Connection;

public class ConnectionManager
{
    public static Connection connectToAnyDb(){
        ConnectionHandler handler = new RemoteConnectionHandler();
        return handler.connect();
    }

    public static Connection reconnectToRemoteDb(){
        ConnectionHandler handler = new ServiceReconnectionHandler();
        return handler.connect();
    }

    public static Connection connectToLocalDb() {
        ConnectionHandler handler = new LocalConnectionHandler();
        return handler.connect();
    }
}



/*
sudo service mysql status
sudo service mysql stop
sudo service mysql status
sudo service mysql restart
 */


/*
Relevant notes;
full url:
jdbc:mysql://localhost:3306/?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Warsaw

solution above is preferred according mySQL documentation
final String useCPCDB = "USE " + DBNAME
PreparedStatement useCPCDBSqlQuery = CONNECTION.prepareStatement(useCPCDB);
useCPCDBSqlQuery.execute();

better use:
CONNECTION.setCatalog( DBNAME );
 */