package com.github.malyszaryczlowiek.cpcdb.alerts;

import javafx.scene.control.Alert;

public abstract class InfoAlert implements ShortAlert
{
    Alert alert;

    InfoAlert()
    {
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setResizable(true);
    }

    @Override
    public void show()
    {
        alert.showAndWait();
    }
}
