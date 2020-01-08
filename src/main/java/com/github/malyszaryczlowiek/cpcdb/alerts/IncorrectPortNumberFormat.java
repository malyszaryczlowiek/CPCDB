package com.github.malyszaryczlowiek.cpcdb.alerts;

public class IncorrectPortNumberFormat extends ErrorAlert
{
    public IncorrectPortNumberFormat(){
        super();
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
