package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

/**
 * klasa ma budować query służące do połączenia się z serwerem po czym ma zwracać text tego query.
 */
public class RemoteConnectionQueryBuilder extends ConnectionQueryBuilder
{
    RemoteConnectionQueryBuilder() {
        super();
        urlBuilder = new StringBuilder("jdbc:")
                .append(SecureProperties.getProperty("settings.db.remote.RDBMS")) // RDBMS - relational database management system e.g. MySQL
                .append("://")
                .append(SecureProperties.getProperty("settings.db.remote.serverAddressIP"))
                .append(":")
                .append(SecureProperties.getProperty("settings.db.remote.portNumber"))
                .append("/");
    }

    @Override
    void addConnectionConfigurations() {
        if ( SecureProperties.getProperty("remoteDBExists").equals("true") ) urlBuilder.append(DBNAME); // dodaję nazwę bazy danych, nie tabeli
        if ( SecureProperties
                .getProperty("settings.db.remote.connectorConfiguration.useRemoteConnectorServerSettings")
                .equals("true") ) {
            urlBuilder.append("?")
                    .append("useUnicode")
                    .append("=")
                    .append(SecureProperties.getProperty("settings.db.remote.connectorConfiguration.useUnicode"))
                    .append("&")
                    .append("useJDBCCompliantTimezoneShift")
                    .append("=")
                    .append(SecureProperties.getProperty("settings.db.remote.connectorConfiguration.useJDBCCompilantTimezoneShift"))
                    .append("&")
                    .append("useLegacyDatetimeCode")
                    .append("=")
                    .append(SecureProperties.getProperty("settings.db.remote.connectorConfiguration.useLegacyDateTimeCode"))
                    .append("&")
                    .append("serverTimezone")
                    .append("=")
                    .append(SecureProperties.getProperty("settings.db.remote.connectorConfiguration.serverTimezone"));
        }
    }

    @Override
    String getQuery() {
        return urlBuilder.toString();
    }
}
