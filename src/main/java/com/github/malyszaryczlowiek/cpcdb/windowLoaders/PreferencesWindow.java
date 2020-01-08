package com.github.malyszaryczlowiek.cpcdb.windowLoaders;

import com.github.malyszaryczlowiek.cpcdb.controllers.SettingsStageController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class PreferencesWindow
{
    public PreferencesWindow() {
        try {
            Stage preferencesStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("settingsStage.fxml"));
            Parent root = loader.load();
            SettingsStageController controller = loader.getController(); // casting on (SearchCompoundStageController)
            Scene scene = new Scene(root);
            preferencesStage.setScene(scene);
            preferencesStage.initModality(Modality.APPLICATION_MODAL);
            preferencesStage.setTitle("Settings");
            preferencesStage.setResizable(true);
            preferencesStage.sizeToScene();
            preferencesStage.setAlwaysOnTop(false);
            // solution taken from:
            // https://stackoverflow.com/questions/13246211/javafx-how-to-get-stage-from-controller-during-initialization
            controller.setStage( preferencesStage );

            preferencesStage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
