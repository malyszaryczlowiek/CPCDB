package com.github.malyszaryczlowiek.cpcdb.windows.alertWindows;

import javafx.scene.control.Alert;

public class FloatNumberFormatError extends AlertWindow
{
    public FloatNumberFormatError(Alert.AlertType alertType) {
        super(alertType);
        alert.setTitle("Error");
        alert.setHeaderText("Incorrect input type.");
        alert.setContentText("Input must be in number format.");
    }
}