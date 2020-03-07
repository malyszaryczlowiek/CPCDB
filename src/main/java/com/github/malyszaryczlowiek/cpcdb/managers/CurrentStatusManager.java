package com.github.malyszaryczlowiek.cpcdb.managers;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ErrorType;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ShortAlertWindowFactory;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Class is thread-save. Due to possession of Text and ProgressBar references, methods modifying these references
 * can be called only in JavaFX Main Thread. Usually this is done in message and progress listeners of using Tasks.
 */
public class CurrentStatusManager
{
    private static Text currentStatus;
    private static ProgressBar progressBar;
    private static double progressValue = 0.0;

    public static CurrentStatusManager getThisCurrentStatusManager() throws NullPointerException {
        if (currentStatus == null) throw new NullPointerException(
                "You must set Text object first. Call getThisCurrentStatusManager(Text text) method instead.");
        else return new CurrentStatusManager();
    }

    public static CurrentStatusManager getThisCurrentStatusManager(Text status, ProgressBar progressB)  {
        if (currentStatus == null || progressBar == null) {
            currentStatus = status;
            progressBar = progressB;
        }
        return new CurrentStatusManager();
    }

    private CurrentStatusManager() {}

    private void resetFont() {
        currentStatus.setFont(Font.getDefault());
        currentStatus.setFill(Paint.valueOf("black"));
    }

    public synchronized void setWarningStatus(String warning) {
        currentStatus.setFont(Font.font("System", FontWeight.BOLD, 13));
        currentStatus.setFill(Paint.valueOf("orange"));
        currentStatus.setText(warning);
        progressValue = 0.0;
        progressBar.setProgress(progressValue);
    }

    public synchronized void setErrorStatus(String error) {
        currentStatus.setFont(Font.font("System", FontWeight.BOLD, 13));
        currentStatus.setFill(Paint.valueOf("red"));
        currentStatus.setText(error);
        progressValue = 0.0;
        progressBar.setProgress(progressValue);
    }

    public synchronized void setInfoStatus(String info) {
        resetFont();
        progressBar.setProgress(progressValue);
        currentStatus.setText(info);
    }

    public synchronized void onCurrentStatusTextClicked() {
        String text = currentStatus.getText();
        if ( text.contains("Warning") || text.contains("Error") ) {
            boolean connectionToRemoteDb = ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR);
            boolean connectionToLocalDb = ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR);
            boolean incorrectRemotePassphrase = ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR);
            boolean incorrectLocalPassphrase = ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR);
            if (connectionToRemoteDb && connectionToLocalDb )
                ShortAlertWindowFactory.showWindow(ErrorType.CANNOT_CONNECT_TO_ALL_DB);
            else if ( connectionToRemoteDb && incorrectLocalPassphrase )
                ShortAlertWindowFactory.showWindow(ErrorType.CANNOT_CONNECT_TO_REMOTE_BD_AND_INCORRECT_LOCAL_PASSPHRASE);
            else if ( !incorrectLocalPassphrase && !connectionToLocalDb && connectionToRemoteDb )
                ShortAlertWindowFactory.showWindow(ErrorType.CANNOT_CONNECT_TO_REMOTE_DB);
            else if ( incorrectRemotePassphrase && connectionToLocalDb )
                ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_REMOTE_PASSPHRASE_AND_CANNOT_CONNECT_TO_LOCAL_DB);
            else if ( incorrectRemotePassphrase && incorrectLocalPassphrase )
                ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_REMOTE_AND_LOCAL_PASSPHRASE);
            else if ( !incorrectLocalPassphrase && !connectionToLocalDb && incorrectRemotePassphrase )
                ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_REMOTE_PASSPHRASE);
        }
        resetFont();
    }

    public synchronized void addToProgressValue(double addToProgressBar) {
        progressValue += addToProgressBar;
        progressBar.setProgress(progressValue);
    }

    public synchronized void setProgressValue(double newValue) { progressValue = newValue; }
}

//public synchronized void updateProgressBar() { progressBar.setProgress( progressValue ); }



    /*
    public void setGreenFont() {
        currentStatus.setFont(Font.font("System", FontWeight.BOLD, 13));
        currentStatus.setFont(Font.getDefault());
        currentStatus.setFill(Paint.valueOf("green"));
    }
     */

