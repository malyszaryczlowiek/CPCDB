package com.github.malyszaryczlowiek.cpcdb.alertWindows;

import javafx.scene.control.Alert;

public class FatalDbConnectionError extends AlertWindow
{
    public FatalDbConnectionError(Alert.AlertType alertType) {
        super(alertType);
        alert.setWidth(750);
        alert.setHeight(550);
        alert.setTitle("Fatal Connection Error");
        alert.setHeaderText("Cannot connect neither remote nor local server.");
        alert.setContentText("Please check your Internet connection. If connection works correctly, please" +
                "contact Server Administrator. \n\nFor local server check if MySQL server is installed. " +
                "If so, check whether is started. For more info please contact System Administrator.");
    }
}

