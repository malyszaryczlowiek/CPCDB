package com.github.malyszaryczlowiek.cpcdb.windowLoaders;

import com.github.malyszaryczlowiek.cpcdb.controllers.SqlPropertiesStageController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class InitializingDBPropertiesWindow
{
    public InitializingDBPropertiesWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("sqlLoadingPropertiesStage.fxml"));
            Parent root = loader.load();
            // SqlPropertiesStageController controller =
            // (SqlPropertiesStageController) loader.getController();
            SqlPropertiesStageController controller = loader.getController();

            Stage sqlPropertiesStage = new Stage();
            sqlPropertiesStage.setTitle("Set Database Connection Properties");
            sqlPropertiesStage.setScene(new Scene(root));
            sqlPropertiesStage.setResizable(true);
            sqlPropertiesStage.sizeToScene();
            controller.setStage(sqlPropertiesStage);
            sqlPropertiesStage.showAndWait();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
