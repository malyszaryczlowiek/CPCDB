package com.github.malyszaryczlowiek.cpcdb.alertWindows;

import javafx.scene.control.Alert;

public class NoFoundCompound extends AlertWindow
{
    public NoFoundCompound(Alert.AlertType alertType) {
        super(alertType);
        alert.setWidth(700);
        alert.setHeight(500);
        alert.setTitle("Information");
        alert.setHeaderText("There was no matching compounds!");
        alert.setContentText("There is no matching compounds for selected criteria.");
    }
}
