package com.github.malyszaryczlowiek.cpcdb.alertWindows;

import javafx.scene.control.Alert;

public class CannotConnectToRemoteAndIncorrectLocalUsernameOrPassphrase extends AlertWindow
{
    public CannotConnectToRemoteAndIncorrectLocalUsernameOrPassphrase(Alert.AlertType alertType) {
        super(alertType);
        alert.setWidth(750);
        alert.setHeight(550);
        alert.setTitle("Fatal Connection Errors");
        alert.setHeaderText("Cannot connect to Databases");
        alert.setContentText("Cannot connect to Remote Database and set incorrect Username or Passphrase " +
                "to Local Database. Please check internet connection and reset username and/or passphrase.");
    }
}
