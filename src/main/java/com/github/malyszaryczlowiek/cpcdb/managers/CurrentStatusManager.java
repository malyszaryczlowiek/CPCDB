package com.github.malyszaryczlowiek.cpcdb.managers;

import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

// TODO można używać locka na tę klasę  CurrentStatusManager.class
public class CurrentStatusManager
{
    private static Text currentStatus;

    public CurrentStatusManager(Text status) {
        currentStatus = status;
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
}
