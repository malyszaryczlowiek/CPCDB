package com.github.malyszaryczlowiek.cpcdb.alerts;

public class FatalDbConnectionError extends ErrorAlert
{
    public FatalDbConnectionError()
    {
        super();
        alert.setWidth(750);
        alert.setHeight(550);
        alert.setTitle("Fatal Connection Error");
        alert.setHeaderText("Cannot connect neither remote nor local server.");
        alert.setContentText("Please check your Internet connection. If connection works correctly, please" +
                "contact Server Administrator. \n\nFor local server check if MySQL server is installed. " +
                "If so, check whether is started. For more info please contact System Administrator.");
    }
}

