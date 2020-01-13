package com.github.malyszaryczlowiek.cpcdb.alertWindows;

import javafx.scene.control.Alert;

public class IncorrectRemotePassphrase extends AlertWindow
{
    public IncorrectRemotePassphrase(Alert.AlertType alertType) {
        super(alertType);
        alert.setTitle("Warning");
        alert.setHeaderText("Incorrect Username or Passphrase for Remote Server.");
        alert.setContentText("You are working now at Local Database. Please check Username and/or Passphrase for Remote Server.");
    }
}
