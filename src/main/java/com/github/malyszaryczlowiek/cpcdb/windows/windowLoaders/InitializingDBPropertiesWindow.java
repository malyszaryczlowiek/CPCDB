package com.github.malyszaryczlowiek.cpcdb.windows.windowLoaders;

import com.github.malyszaryczlowiek.cpcdb.controllers.SqlPropertiesStageController;
import com.github.malyszaryczlowiek.cpcdb.windows.ShowAble;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

class InitializingDBPropertiesWindow implements ShowAble
{
    private  Stage sqlPropertiesStage;

    InitializingDBPropertiesWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("sqlLoadingPropertiesStage.fxml"));
        Parent root = loader.load();
        // SqlPropertiesStageController controller =
        // (SqlPropertiesStageController) loader.getController();
        SqlPropertiesStageController controller = loader.getController();

        sqlPropertiesStage = new Stage();
        sqlPropertiesStage.setTitle("Set Database Connection Properties");
        sqlPropertiesStage.setScene(new Scene(root));
        sqlPropertiesStage.setResizable(true);
        sqlPropertiesStage.sizeToScene();
        controller.setStage(sqlPropertiesStage);
    }

    @Override
    public void show() {
        sqlPropertiesStage.showAndWait();
    }
}
