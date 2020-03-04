package com.github.malyszaryczlowiek.cpcdb.managers;

import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Class is thread-save. Due to possession of Text and ProgressBar references it can be used
 * only in JavaFX Main Thread.
 */
public class CurrentStatusManager
{
    private static Text currentStatus;
    private static ProgressBar progressBar;
    //private CurrentStatusManager thisCurrentStatusManager;

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

    public synchronized void setCurrentStatus(String text) { currentStatus.setText(text); }

    public synchronized void setErrorMessage(String error) {
        currentStatus.setFont(Font.font("System", FontWeight.BOLD, 13));
        currentStatus.setFill(Paint.valueOf("red"));
        currentStatus.setText(error);
    }

    public synchronized void setWarningMessage(String warning) {
        currentStatus.setFont(Font.font("System", FontWeight.BOLD, 13));
        currentStatus.setFill(Paint.valueOf("orange"));
        currentStatus.setText(warning);
    }

    public synchronized void resetFont() {
        currentStatus.setFont(Font.getDefault());
        currentStatus.setFill(Paint.valueOf("black"));
    }

    public synchronized void setErrorStatus(String error, double progressBarValue) {
            currentStatus.setFont(Font.font("System", FontWeight.BOLD, 13));
            currentStatus.setFill(Paint.valueOf("red"));
            currentStatus.setText(error);
            progressBar.setProgress(progressBarValue);
    }

    public synchronized void setInfoStatus(String info, double progressBarValue) {
        resetFont();
        currentStatus.setText(info);
        progressBar.setProgress(progressBarValue);
    }
}

    /*
    public void setGreenFont() {
        currentStatus.setFont(Font.font("System", FontWeight.BOLD, 13));
        currentStatus.setFont(Font.getDefault());
        currentStatus.setFill(Paint.valueOf("green"));
    }
     */

