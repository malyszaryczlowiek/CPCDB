package com.github.malyszaryczlowiek.cpcdb.windows.windowLoaders;

import com.github.malyszaryczlowiek.cpcdb.controllers.ColumnManagerStageController;

import com.github.malyszaryczlowiek.cpcdb.windows.ShowAble;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

class ColumnManagerWindow implements ShowAble
{
    private Stage columnManagerStage;

    ColumnManagerWindow() throws IOException {
             columnManagerStage = new Stage();
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
    }

    @Override
    public void show() {
        columnManagerStage.show();
    }
}
