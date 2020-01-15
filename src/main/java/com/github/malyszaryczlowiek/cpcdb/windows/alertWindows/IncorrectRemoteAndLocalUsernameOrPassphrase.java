package com.github.malyszaryczlowiek.cpcdb.windows.alertWindows;

import javafx.scene.control.Alert;

class IncorrectRemoteAndLocalUsernameOrPassphrase extends AlertWindow
{
    IncorrectRemoteAndLocalUsernameOrPassphrase(Alert.AlertType alertType) {
        super(alertType);
        alert.setTitle("Fatal Connection Error");
        alert.setHeaderText("Incorrect Username or Passphrase.");
        alert.setContentText("Please check Username and/or Passphrase for Local and Remote Servers.");
    }
}
