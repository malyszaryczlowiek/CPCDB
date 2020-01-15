package com.github.malyszaryczlowiek.cpcdb.windows.windowLoaders;

import com.github.malyszaryczlowiek.cpcdb.controllers.AddCompoundStageController;
import com.github.malyszaryczlowiek.cpcdb.controllers.MainStageController;

import com.github.malyszaryczlowiek.cpcdb.windows.ShowAble;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

class AddCompoundWindow implements ShowAble
{
    private Stage addCompoundStage;

    AddCompoundWindow(MainStageController mainStageController) throws IOException
    {
        addCompoundStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("addCompoundStage.fxml"));
        Parent root = loader.load();
        AddCompoundStageController controller = loader.getController(); // casting on (AddCompoundStageController)
        Scene scene = new Scene(root);
        addCompoundStage.setScene(scene);
        addCompoundStage.initModality(Modality.APPLICATION_MODAL);
        addCompoundStage.setTitle("Add Compound");
        addCompoundStage.setMinHeight( 370 + 30);
        addCompoundStage.setMinWidth(770);
        addCompoundStage.setResizable(true);
        //addCompoundStage.setAlwaysOnTop(true);
        // solution taken from:
        // https://stackoverflow.com/questions/13246211/javafx-how-to-get-stage-from-controller-during-initialization
        controller.setStage(addCompoundStage);
        controller.setMainStageControllerObject(mainStageController);
    }

    @Override
    public void show() {
        addCompoundStage.show();
    }
}
