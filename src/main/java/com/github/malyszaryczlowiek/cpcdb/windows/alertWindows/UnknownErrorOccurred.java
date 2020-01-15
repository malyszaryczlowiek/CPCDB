package com.github.malyszaryczlowiek.cpcdb.windows.alertWindows;

import javafx.scene.control.Alert;

class UnknownErrorOccurred extends AlertWindow
{
    UnknownErrorOccurred(Alert.AlertType alertType) {
        super(alertType);
        alert.setTitle("Unknown Error");
        alert.setHeaderText("Something Went Very Very Wrong");
        alert.setContentText("Check Log File to detect problem.");
    }
}
