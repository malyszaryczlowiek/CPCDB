package com.github.malyszaryczlowiek.cpcdb.alerts;

import javafx.scene.control.Alert;

public abstract class ErrorAlert implements ShortAlert
{
    Alert alert;

    ErrorAlert()
    {
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setResizable(true);
    }

    @Override
    public void show()
    {
        alert.show();
    }
}
