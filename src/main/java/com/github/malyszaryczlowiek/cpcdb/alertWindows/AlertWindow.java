package com.github.malyszaryczlowiek.cpcdb.alertWindows;

import javafx.scene.control.Alert;

public abstract class AlertWindow implements ShortAlert
{
    Alert alert;

    AlertWindow(Alert.AlertType alertType) {
        alert = new Alert(alertType);
        alert.setResizable(true);
    }

    @Override
    public void show()
    {
        alert.show();
    }
}
