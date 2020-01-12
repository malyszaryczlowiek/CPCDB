package com.github.malyszaryczlowiek.cpcdb.alertWindows;

import javafx.scene.control.Alert;

public class IncorrectPortNumberFormat extends AlertWindow
{
    public IncorrectPortNumberFormat(Alert.AlertType alertType) {
        super(alertType);
        alert.setWidth(700);
        alert.setHeight(400);
        alert.setTitle("Error");
        alert.setHeaderText("Incorrect Remote or Local Port Number Format.");
        alert.setContentText("Port Number should be integer number.");
    }

    @Override
    public void show() {
        alert.showAndWait();
    }
}
