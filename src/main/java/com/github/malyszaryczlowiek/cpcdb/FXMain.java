package com.github.malyszaryczlowiek.cpcdb;

import com.github.malyszaryczlowiek.cpcdb.controllers.MainStageController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class FXMain extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainStage.fxml"));
        Parent root = loader.load();
        MainStageController controller =  loader.getController(); // casting (MainStageController)

        primaryStage.setTitle("CPCDB");
        Scene scene = new Scene(root, 1900, 1000);
        scene.getStylesheets().add("myStyle.css");
        primaryStage.setScene(scene);
        primaryStage.setFullScreenExitHint("Exit full screen mode: Esc");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("Esc"));
        primaryStage.setMaxWidth(4100);
        primaryStage.setResizable(true);
        primaryStage.sizeToScene();
        primaryStage.setMaximized(true);
        controller.setStage(primaryStage); // there is hidden primaryStage.show()

        //primaryStage.setIconified(true);
        //primaryStage.setOnCloseRequest(e -> Platform.exit());
        //primaryStage.centerOnScreen();
        // primaryStage.show(); // this method is called in controller.setStage()
    }

    public static void main(String[] args) {
        launch(args);
    }
}

