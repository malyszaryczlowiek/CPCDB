package com.github.malyszaryczlowiek.cpcdb.alertWindows;

import javafx.scene.control.Alert;

public class IncorrectRemoteAndLocalUsernameOrPassphrase extends AlertWindow
{
    public IncorrectRemoteAndLocalUsernameOrPassphrase(Alert.AlertType alertType) {
        super(alertType);
        alert.setWidth(750);
        alert.setHeight(550);
        alert.setTitle("Fatal Connection Error");
        alert.setHeaderText("Incorrect Username or Passphrase.");
        alert.setContentText("Please check Username and/or Passphrase for Local and Remote Servers.");
    }
}
