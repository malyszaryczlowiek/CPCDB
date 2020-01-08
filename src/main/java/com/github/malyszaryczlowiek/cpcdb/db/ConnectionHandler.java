package com.github.malyszaryczlowiek.cpcdb.db;

import java.sql.Connection;

interface ConnectionHandler
{
    String DBNAME = "Wa1s8JBvyU";
    Connection connect();
}

/*
private static void createChangesTableIfNotExists()
    {
        Connection connectionToLocalDB;
        StringBuilder urlBuilder = new StringBuilder("jdbc:")
                .append(SecureProperties.getProperty("settings.db.local.RDBMS")) // RDBMS - relational database management system
                .append("://")
                .append("localhost")
                .append(":")
                .append("3306")
                .append("/");

        try
        {
            if (SecureProperties.hasProperty("localDBExists"))
                urlBuilder.append(DBNAME);
            String dbConfiguration = SecureProperties.getProperty("settings.db.local.serverConfiguration");
            if (!dbConfiguration.equals(""))
                urlBuilder.append("?").append(dbConfiguration);

            connectionToLocalDB = DriverManager.getConnection(
                    urlBuilder.toString(),
                    SecureProperties.getProperty("settings.db.local.user"),
                    SecureProperties.getProperty("settings.db.local.passphrase"));

            final String databaseExistSQLQuery = "CREATE DATABASE IF NOT EXISTS " + DBNAME;
            PreparedStatement createDBifNotExist = connectionToLocalDB.prepareStatement(databaseExistSQLQuery);
            createDBifNotExist.execute();

            connectionToLocalDB.setCatalog(DBNAME);

            //  check if compound table exists in our DB
            final String checkIfTableExistsInDBSqlQuery = "SELECT * " +
                    "FROM information_schema.tables " +
                    "WHERE table_schema = '" + DBNAME + "' " +
                    "AND table_name = 'changes' " +
                    "LIMIT 10";

            PreparedStatement checkTableExists = connectionToLocalDB.prepareStatement(checkIfTableExistsInDBSqlQuery);
            ResultSet rs = checkTableExists.executeQuery();
            if (!rs.last())
            {
                final String sqlQueryCreateTable = "CREATE TABLE " + //"IF NOT EXISTS " +
                        "changes(" +
                        "CompoundID INT NOT NULL, " +
                        //"Smiles VARCHAR(255) NOT NULL, " +
                        //"CompoundNumber VARCHAR(255), " +
                        "Amount FLOAT, " +
                        //"Unit VARCHAR(255) CHARACTER SET utf8, " +
                        //"Form VARCHAR(255) CHARACTER SET utf8, " +
                        //"Stability VARCHAR(255) CHARACTER SET utf8, " +
                        //"Argon BOOLEAN, " +
                        //"Container VARCHAR(255) CHARACTER SET utf8, " +
                        //"StoragePlace VARCHAR(255) CHARACTER SET utf8, " +
                        //"LastModification TIMESTAMP(0), " +
                        //"AdditionalInfo TEXT CHARACTER SET utf8, " +
                        //"PRIMARY KEY (CompoundID)" +
                        ")";

                PreparedStatement createTable = connectionToLocalDB.prepareStatement(sqlQueryCreateTable);
                createTable.execute();
                SecureProperties.setProperty("changesTableExists", "true");
            }
        }
        catch (com.mysql.cj.jdbc.exceptions.CommunicationsException e)
        {
            // TODO tu będize trzeba poinformować użytkownika, że nie można się połączyć
            System.out.println(" Local Mysql server is turn off. ");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        System.out.println("table 'changes' is created correctly.");
    }
 */