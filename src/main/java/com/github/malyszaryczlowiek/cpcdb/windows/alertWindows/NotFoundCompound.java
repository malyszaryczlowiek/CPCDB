package com.github.malyszaryczlowiek.cpcdb.windows.alertWindows;

import javafx.scene.control.Alert;

class NotFoundCompound extends AlertWindow
{
    NotFoundCompound(Alert.AlertType alertType) {
        super(alertType);
        alert.setTitle("Information");
        alert.setHeaderText("There was no matching compounds!");
        alert.setContentText("There is no matching compounds for selected criteria.");
    }
}
