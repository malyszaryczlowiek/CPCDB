package com.github.malyszaryczlowiek.cpcdb.windows.alertWindows;

import javafx.scene.control.Alert;

class CannotConnectToRemoteAndIncorrectLocalUsernameOrPassphrase extends AlertWindow
{
    CannotConnectToRemoteAndIncorrectLocalUsernameOrPassphrase(Alert.AlertType alertType) {
        super(alertType);
        alert.setTitle("Fatal Connection Errors");
        alert.setHeaderText("Cannot connect to Databases");
        alert.setContentText("Cannot connect to Remote Database and set incorrect Username or Passphrase " +
                "to Local Database. Please check internet connection and reset username and/or passphrase.");
    }
}
