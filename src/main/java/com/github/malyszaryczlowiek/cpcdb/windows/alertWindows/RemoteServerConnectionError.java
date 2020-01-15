package com.github.malyszaryczlowiek.cpcdb.windows.alertWindows;

import javafx.scene.control.Alert;

class RemoteServerConnectionError extends AlertWindow
{
    RemoteServerConnectionError(Alert.AlertType alertType) {
        super(alertType);
        alert.setTitle("Error");
        alert.setHeaderText("Cannot connect to Remote Server.");
        alert.setContentText("Please check your Internet connection. Currently You are working on Local " +
                "copy of Database. If connection works correctly, please" +
                "contact Server Administrator.");
    }
}
/*
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setResizable(true);
                alert.setWidth(700);
                alert.setHeight(400);
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.setTitle("Connection Error");
                alert.setHeaderText("Cannot connect to remote server.");
                alert.setContentText("Please check your Internet connection. Currently You are working on local " +
                        "copy of DataBase. If connection works correctly, please" +
                        "contact Server Administrator.");
                alert.show();

                 */