package com.github.malyszaryczlowiek.cpcdb.alertWindows;

import javafx.scene.control.Alert;

public class RemoteServerPassphraseErrorAndCannotConnectToLocalDatabase extends AlertWindow
{
    public RemoteServerPassphraseErrorAndCannotConnectToLocalDatabase(Alert.AlertType alertType) {
        super(alertType);
        alert.setTitle("Fatal Connection Error");
        alert.setHeaderText("Incorrect Username or Passphrase for Remote Server and Connection Error to Local Database.");
        alert.setContentText("Please check Username and/or Passphrase for Remote Server and check if MySQL Server is installed on your machine.");
    }
}
