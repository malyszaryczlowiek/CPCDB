package com.github.malyszaryczlowiek.cpcdb.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class DatabaseAndTableCreator
{
    static void createIfNotExist(Connection CONNECTION, DatabaseLocation location) throws SQLException {
        final String databaseExistSQLQuery = "CREATE DATABASE IF NOT EXISTS " + ConnectionHandler.DBNAME;
        PreparedStatement createDBifNotExist = CONNECTION.prepareStatement(databaseExistSQLQuery);
        createDBifNotExist.execute();

        CONNECTION.setCatalog(ConnectionHandler.DBNAME);

        final String checkIfTableExistsInDBSqlQuery = "SELECT * " +
                "FROM information_schema.tables " +
                "WHERE table_schema = '" + ConnectionHandler.DBNAME + "' " +
                "AND table_name = 'compounds' " +
                "LIMIT 10";
        PreparedStatement checkTableExists = CONNECTION.prepareStatement( checkIfTableExistsInDBSqlQuery );
        ResultSet rs = checkTableExists.executeQuery();
        if (!rs.last()) { // if table does not exist, create it
            final String sqlQueryCreateTable ;
            if ( location.equals(DatabaseLocation.REMOTE)) {
                sqlQueryCreateTable ="CREATE TABLE " + //"IF NOT EXISTS " +
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
            }
            else {
                sqlQueryCreateTable ="CREATE TABLE " + //"IF NOT EXISTS " +
                        "compounds(" +
                        "CompoundID INT NOT NULL, " +
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
            }
            PreparedStatement createTable = CONNECTION.prepareStatement(sqlQueryCreateTable);
            createTable.execute();
        }
    }
}
