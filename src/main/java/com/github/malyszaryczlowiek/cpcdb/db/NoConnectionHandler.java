package com.github.malyszaryczlowiek.cpcdb.db;

import java.sql.Connection;

class NoConnectionHandler implements ConnectionHandler
{
    @Override
    public Connection connect() {
        return null;
    }
}
