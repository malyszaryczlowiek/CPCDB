package com.github.malyszaryczlowiek.cpcdb.windowLoaders;

import com.github.malyszaryczlowiek.cpcdb.controllers.AskToSaveChangesBeforeQuitController;
import com.github.malyszaryczlowiek.cpcdb.controllers.MainStageController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SaveChangesBeforeQuitWindow
{
    public SaveChangesBeforeQuitWindow(MainStageController mainStageController) {
        Stage askToSaveChangesBeforeQuit = new Stage();
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("askToSaveChangesBeforeQuitStage.fxml")
        );
        try {
            Parent root = loader.load();
            AskToSaveChangesBeforeQuitController controller
                    = loader.getController();
            // casting on (AskToSaveChangesBeforeQuitController)
            Scene scene = new Scene(root, 605, 100);
            askToSaveChangesBeforeQuit.setScene(scene);
            askToSaveChangesBeforeQuit.initModality(Modality.APPLICATION_MODAL);
            askToSaveChangesBeforeQuit.setTitle("Save Changes?");
            askToSaveChangesBeforeQuit.sizeToScene();
            askToSaveChangesBeforeQuit.setMinHeight(135);
            askToSaveChangesBeforeQuit.setMinWidth(610);
            askToSaveChangesBeforeQuit.setHeight(135);
            askToSaveChangesBeforeQuit.setWidth(610);
            askToSaveChangesBeforeQuit.setMaxHeight(135);
            askToSaveChangesBeforeQuit.setMaxWidth(610);
            askToSaveChangesBeforeQuit.setResizable(true);
            askToSaveChangesBeforeQuit.setAlwaysOnTop(false);
            // solution taken from:
            // https://stackoverflow.com/questions/13246211/javafx-how-to-get-stage-from-controller-during-initialization
            controller.setStage(askToSaveChangesBeforeQuit);
            controller.setMainStageControllerObject(mainStageController);

            askToSaveChangesBeforeQuit.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
