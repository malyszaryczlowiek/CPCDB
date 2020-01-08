package com.github.malyszaryczlowiek.cpcdb.alerts;

public class FloatNumberFormatError extends ErrorAlert
{
    public FloatNumberFormatError()
    {
        super();
        alert.setWidth(700);
        alert.setHeight(400);
        alert.setTitle("Error");
        alert.setHeaderText("Incorrect input type.");
        alert.setContentText("Input must be in number format.");
    }
}
                            /*
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setResizable(true);
                            alert.setWidth(700);
                            alert.setHeight(400);
                            alert.setTitle("Error");
                            alert.setHeaderText("Incorrect input type.");
                            alert.setContentText("Input must be in number format.");
                            alert.showAndWait();
                             */