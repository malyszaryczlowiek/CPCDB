package com.github.malyszaryczlowiek.cpcdb.windowLoaders;

import com.github.malyszaryczlowiek.cpcdb.controllers.ColumnManagerStageController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ColumnManagerWindow
{
    public ColumnManagerWindow() {
        try {
            Stage columnManagerStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("columnManagerStage.fxml"));
            Parent root = loader.load();
            ColumnManagerStageController controller = loader.getController();

            columnManagerStage.setTitle("Select Columns");
            columnManagerStage.setScene(new Scene(root));
            //columnManagerStage.setMinHeight(355+30);
            //columnManagerStage.setMinWidth(755);
            columnManagerStage.setResizable(false);
            columnManagerStage.sizeToScene();
            controller.setStage(columnManagerStage);
            columnManagerStage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
