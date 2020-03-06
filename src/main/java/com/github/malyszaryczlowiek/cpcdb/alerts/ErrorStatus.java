package com.github.malyszaryczlowiek.cpcdb.alerts;

public class ErrorStatus
{
    private Boolean errorOccurred;
    private String errorMessage;

    ErrorStatus() {
        this.errorOccurred = false;
        this.errorMessage = "";
    }

    /*
    ErrorStatus(Boolean errorOccurred, String errorMessage) {
        this.errorOccurred = errorOccurred;
        this.errorMessage = errorMessage;
    }
     */

    void resetStatus() {
        errorOccurred = false;
        errorMessage = "";
    }

    void setErrorMessage(String errorMessage) {
        this.errorOccurred = true;
        this.errorMessage = errorMessage;
    }

    public Boolean getErrorOccurred() { return errorOccurred; }

    public String getErrorMessage() { return errorMessage; }
}
