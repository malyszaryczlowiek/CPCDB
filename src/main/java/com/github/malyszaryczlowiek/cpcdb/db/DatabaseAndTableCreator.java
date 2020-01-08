package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class DatabaseAndTableCreator
{
    DatabaseAndTableCreator(Connection CONNECTION, String DBNAME, DatabaseLocation location) throws SQLException {
        final String databaseExistSQLQuery = "CREATE DATABASE IF NOT EXISTS " + DBNAME;
        PreparedStatement createDBifNotExist = CONNECTION.prepareStatement(databaseExistSQLQuery);
        createDBifNotExist.execute();

        CONNECTION.setCatalog(DBNAME);

        final String checkIfTableExistsInDBSqlQuery = "SELECT * " +
                "FROM information_schema.tables " +
                "WHERE table_schema = '" + DBNAME + "' " +
                "AND table_name = 'compounds' " +
                "LIMIT 10";

        PreparedStatement checkTableExists = CONNECTION.prepareStatement( checkIfTableExistsInDBSqlQuery );
        ResultSet rs = checkTableExists.executeQuery();
        if (!rs.last()) { // jeśli nie istnieje stwórz tabelę
            final String sqlQueryCreateTable = "CREATE TABLE " + //"IF NOT EXISTS " +
                    "compounds(" +
                    "CompoundID INT NOT NULL AUTO_INCREMENT, " +
                    "Smiles VARCHAR(255) NOT NULL, " +
                    "CompoundNumber VARCHAR(255), " +
                    "Amount FLOAT, " +
                    "Unit VARCHAR(255) CHARACTER SET utf8, " +
                    "Form VARCHAR(255) CHARACTER SET utf8, " +
                    "Stability VARCHAR(255) CHARACTER SET utf8, " +
                    "Argon BOOLEAN, " +
                    "Container VARCHAR(255) CHARACTER SET utf8, " +
                    "StoragePlace VARCHAR(255) CHARACTER SET utf8, " +
                    "LastModification TIMESTAMP(0), " +
                    "AdditionalInfo TEXT CHARACTER SET utf8, " +
                    "PRIMARY KEY (CompoundID)" +
                    ")";

            PreparedStatement createTable = CONNECTION.prepareStatement(sqlQueryCreateTable);
            createTable.execute();
            if (location.equals(DatabaseLocation.REMOTE))
                SecureProperties.setProperty("remoteDBExists", "true");
            else
                SecureProperties.setProperty("localDBExists", "true");
            // jeśli powyższe polecenia zostaną wykonane poprawnie, to możemy zoznaczyć że baza jest stworzona
        }
    }
}
