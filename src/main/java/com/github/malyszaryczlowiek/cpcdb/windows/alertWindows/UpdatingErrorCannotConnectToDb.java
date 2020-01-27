package com.github.malyszaryczlowiek.cpcdb.windows.alertWindows;

import javafx.scene.control.Alert;

public class UpdatingErrorCannotConnectToDb extends AlertWindow
{
    UpdatingErrorCannotConnectToDb(Alert.AlertType alertType) {
        super(alertType);
        alert.setTitle("Error");
        alert.setHeaderText("Cannot Update Local Database");
        alert.setContentText("Check if Local MysQL Server is started/working and then restart program to update Local Database.");
    }
}
