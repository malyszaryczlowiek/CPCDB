package com.github.malyszaryczlowiek.cpcdb.windows.alertWindows;

import javafx.scene.control.Alert;

class IncorrectPortNumberFormat extends AlertWindow
{
    IncorrectPortNumberFormat(Alert.AlertType alertType) {
        super(alertType);
        alert.setTitle("Error");
        alert.setHeaderText("Incorrect Remote or Local Port Number Format.");
        alert.setContentText("Port Number should be integer number.");
    }

    @Override
    public void show() { alert.showAndWait(); }
}
