package com.github.malyszaryczlowiek.cpcdb.db;

import java.sql.Connection;

public class ConnectionManager
{
    public static Connection connectToDb(){
        ConnectionHandler handler = new RemoteConnectionHandler();
        return handler.connect();
    }
}



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