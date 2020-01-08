package com.github.malyszaryczlowiek.cpcdb.windowLoaders;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.controllers.EditCompoundStageController;
import com.github.malyszaryczlowiek.cpcdb.controllers.MainStageController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class EditCompoundWindow
{
    public EditCompoundWindow(MainStageController mainStageController, Compound selectedCompound) throws IOException
    {
        Stage showEditStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("showEditCompoundStage.fxml"));
        Parent root = loader.load();
        EditCompoundStageController controller = loader.getController(); // casting on (EditCompoundStageController)

        showEditStage.setTitle("Edit Compound");
        showEditStage.setScene(new Scene(root));
        showEditStage.setMinHeight(355+30);
        showEditStage.setMinWidth(755);
        //showEditStage.setAlwaysOnTop(true);
        showEditStage.setResizable(true);
        showEditStage.sizeToScene();
        controller.setStage(showEditStage);
        controller.setSelectedItem(selectedCompound);
        controller.setListener(mainStageController);
        showEditStage.show();

    }
}
