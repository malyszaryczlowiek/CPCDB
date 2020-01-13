package com.github.malyszaryczlowiek.cpcdb.alertWindows;

import javafx.scene.control.Alert;

public class UnknownErrorOccurred extends AlertWindow
{
    public UnknownErrorOccurred(Alert.AlertType alertType) {
        super(alertType);
        alert.setTitle("Unknown Error");
        alert.setHeaderText("Something Went Very Very Wrong");
        alert.setContentText("Check Log File to detect problem.");
    }
}
