package com.github.malyszaryczlowiek.cpcdb.windows.alertWindows;

import com.github.malyszaryczlowiek.cpcdb.windows.ShowAble;
import javafx.scene.control.Alert;

public abstract class AlertWindow implements ShowAble
{
    Alert alert;

    AlertWindow(Alert.AlertType alertType) {
        alert = new Alert(alertType);
        alert.setWidth(750);
        alert.setHeight(600);
        alert.setResizable(true);
    }

    @Override
    public void show()
    {
        alert.show();
    }
}
