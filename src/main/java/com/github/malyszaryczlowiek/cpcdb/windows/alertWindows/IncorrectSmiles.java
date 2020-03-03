package com.github.malyszaryczlowiek.cpcdb.windows.alertWindows;

import javafx.scene.control.Alert;

public class IncorrectSmiles extends AlertWindow
{
    IncorrectSmiles(Alert.AlertType alertType) {
        super(alertType);
        alert.setTitle("Error");
        alert.setHeaderText("Incorrect Smiles:");
        alert.setContentText("Smiles cannot be empty.");
    }

    @Override
    public void show() {
        alert.showAndWait();
    }
}
