package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

public class LocalConnectionQueryBuilder extends ConnectionQueryBuilder
{
    LocalConnectionQueryBuilder() {
        super();
        urlBuilder = new StringBuilder("jdbc:")
                .append(SecureProperties.getProperty("settings.db.local.RDBMS")) // RDBMS - relational database management system
                .append("://")
                .append(SecureProperties.getProperty("settings.db.local.serverAddressIP"))
                .append(":")
                .append(SecureProperties.getProperty("settings.db.local.portNumber"))
                .append("/");
    }
    @Override
    void addConnectionConfigurations() {
        if ( SecureProperties.getProperty("localDBExists").equals("true") ) urlBuilder.append(DBNAME); // dodaję nazwę bazy danych, nie nazwę tabeli
        if ( SecureProperties
                .getProperty("settings.db.local.connectorConfiguration.useLocalConnectorServerSettings")
                .equals("true") ) {
            urlBuilder.append("?")
                    .append("useUnicode")
                    .append("=")
                    .append(SecureProperties.getProperty("settings.db.local.connectorConfiguration.useUnicode"))
                    .append("&")
                    .append("useJDBCCompliantTimezoneShift")
                    .append("=")
                    .append(SecureProperties.getProperty("settings.db.local.connectorConfiguration.useJDBCCompilantTimezoneShift"))
                    .append("&")
                    .append("useLegacyDatetimeCode")
                    .append("=")
                    .append(SecureProperties.getProperty("settings.db.local.connectorConfiguration.useLegacyDateTimeCode"))
                    .append("&")
                    .append("serverTimezone")
                    .append("=")
                    .append(SecureProperties.getProperty("settings.db.local.connectorConfiguration.serverTimezone"));
        }
    }

    @Override
    String getQuery() { return urlBuilder.toString(); }
}
