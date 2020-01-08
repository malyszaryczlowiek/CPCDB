package com.github.malyszaryczlowiek.cpcdb.alerts;

public class NoFoundCompound extends InfoAlert
{
    public NoFoundCompound()
    {
        alert.setWidth(700);
        alert.setHeight(500);
        alert.setTitle("Information");
        alert.setHeaderText("There was no matching compounds!");
        alert.setContentText("There is no matching compounds for selected criteria.");
    }
}
