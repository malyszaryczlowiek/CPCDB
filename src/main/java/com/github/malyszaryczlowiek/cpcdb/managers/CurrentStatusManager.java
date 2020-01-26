package com.github.malyszaryczlowiek.cpcdb.managers;

import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

// TODO można używać locka na tę klasę  CurrentStatusManager.class
public class CurrentStatusManager
{
    private static Text currentStatus;
    //private CurrentStatusManager thisCurrentStatusManager;

    public static CurrentStatusManager getThisCurrentStatusManager() throws NullPointerException {
        if (currentStatus == null)
            throw new NullPointerException("You must set Text object first. Call getThisCurrentStatusManager(Text text) method instead.");
        else
            return new CurrentStatusManager();
    }

    public static CurrentStatusManager getThisCurrentStatusManager(Text status)  {
        if (currentStatus == null)
            currentStatus = status;
        return new CurrentStatusManager();
    }

    private CurrentStatusManager() {
    }

    /*
    public Text getCurrentStatus() {
        return currentStatus;
    }
     */

    public void setCurrentStatus(String text) {
        currentStatus.setText(text);
    }

    public void setErrorMessage(String error) {
        currentStatus.setFont(Font.font("System", FontWeight.BOLD, 13));
        currentStatus.setFill(Paint.valueOf("red"));
        currentStatus.setText(error);
    }

    public void setWarningMessage(String warning) {
        currentStatus.setFont(Font.font("System", FontWeight.BOLD, 13));
        currentStatus.setFill(Paint.valueOf("orange"));
        currentStatus.setText(warning);
    }

    public void resetFont() {
        currentStatus.setFont(Font.getDefault());
        currentStatus.setFill(Paint.valueOf("black"));
    }

    /*
    public void setGreenFont() {
        currentStatus.setFont(Font.font("System", FontWeight.BOLD, 13));
        currentStatus.setFont(Font.getDefault());
        currentStatus.setFill(Paint.valueOf("green"));
    }
     */
}
