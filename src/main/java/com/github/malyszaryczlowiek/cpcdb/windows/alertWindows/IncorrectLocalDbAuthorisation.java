package com.github.malyszaryczlowiek.cpcdb.windows.alertWindows;

import javafx.scene.control.Alert;

public class IncorrectLocalDbAuthorisation extends AlertWindow
{
    IncorrectLocalDbAuthorisation(Alert.AlertType alertType) {
        super(alertType);
        alert.setTitle("Error");
        alert.setHeaderText("Authorisation Error. Cannot Update Local Database.");
        alert.setContentText("Please check settings Username and/or Passphrase for Local Servers, and then restart program to update Local Database.");
    }
}
