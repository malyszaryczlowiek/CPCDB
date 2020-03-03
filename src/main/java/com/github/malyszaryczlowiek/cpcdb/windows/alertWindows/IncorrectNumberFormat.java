package com.github.malyszaryczlowiek.cpcdb.windows.alertWindows;

import javafx.scene.control.Alert;

public class IncorrectNumberFormat extends AlertWindow
{
    IncorrectNumberFormat(Alert.AlertType alertType) {
        super(alertType);
        alert.setTitle("Error");
        alert.setHeaderText("Incorrect 'Amount' data type:");
        alert.setContentText("Amount input must have number format.");
    }

    @Override
    public void show() { alert.showAndWait(); }
}
