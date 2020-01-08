package com.github.malyszaryczlowiek.cpcdb.db;

public abstract class ConnectionQueryBuilder
{
    protected String DBNAME = "Wa1s8JBvyU";
    protected StringBuilder urlBuilder;

    abstract void addConnectionConfigurations();
    abstract String getQuery();

}
