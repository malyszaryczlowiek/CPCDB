package com.github.malyszaryczlowiek.cpcdb.windowLoaders;

import com.github.malyszaryczlowiek.cpcdb.controllers.MainStageController;
import com.github.malyszaryczlowiek.cpcdb.controllers.SearchCompoundStageController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SearchCompoundWindow
{
    public  SearchCompoundWindow(MainStageController mainStageController) throws IOException
    {
        Stage searchCompoundStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("findDialogStage.fxml"));
        Parent root = loader.load();
        SearchCompoundStageController controller = loader.getController(); // casting on (SearchCompoundStageController)
        Scene scene = new Scene(root);
        searchCompoundStage.setScene(scene);
        searchCompoundStage.initModality(Modality.APPLICATION_MODAL);
        searchCompoundStage.setTitle("Find Compounds");
        searchCompoundStage.setMinHeight(360 + 30);
        searchCompoundStage.setMinWidth(585);
        searchCompoundStage.setResizable(true);
        searchCompoundStage.setAlwaysOnTop(false);
        // solution taken from:
        // https://stackoverflow.com/questions/13246211/javafx-how-to-get-stage-from-controller-during-initialization
        controller.setStage(searchCompoundStage);
        controller.setMainStageControllerObject(mainStageController);

        searchCompoundStage.show();
    }
}
