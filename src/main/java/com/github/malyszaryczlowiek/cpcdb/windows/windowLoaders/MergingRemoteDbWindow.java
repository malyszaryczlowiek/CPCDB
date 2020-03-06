package com.github.malyszaryczlowiek.cpcdb.windows.windowLoaders;

import com.github.malyszaryczlowiek.cpcdb.controllers.MainStageController;
import com.github.malyszaryczlowiek.cpcdb.controllers.MergingRemoteDbController;
import com.github.malyszaryczlowiek.cpcdb.windows.ShowAble;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

class MergingRemoteDbWindow implements ShowAble
{
    private Stage mergeWindow;

    MergingRemoteDbWindow(MainStageController mainStageController) throws IOException {
        mergeWindow = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mergingRemoteDbStage.fxml"));
        Parent root = loader.load();
        MergingRemoteDbController controller = loader.getController(); // casting on (AddCompoundStageController)
        Scene scene = new Scene(root);
        mergeWindow.setScene(scene);
        mergeWindow.initModality(Modality.APPLICATION_MODAL);
        mergeWindow.setTitle("Merging Remote Database");
        mergeWindow.setResizable(false);
        controller.setStage(mergeWindow);
        controller.setMainStageController(mainStageController);
    }

    @Override
    public void show() { mergeWindow.show(); }
}
