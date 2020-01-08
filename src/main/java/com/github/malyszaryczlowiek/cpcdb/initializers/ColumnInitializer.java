package com.github.malyszaryczlowiek.cpcdb.initializers;

import com.github.malyszaryczlowiek.cpcdb.buffer.ChangesDetector;
import com.github.malyszaryczlowiek.cpcdb.compound.Compound;

import javafx.scene.control.TableView;

public  class ColumnInitializer //implements Initializable
{
     protected static ChangesDetector changesDetector;
     protected static TableView<Compound> mainSceneTableView;

     public static void setUpInitializer(TableView<Compound> tableView, ChangesDetector changesDetector1) {
          changesDetector = changesDetector1;
          mainSceneTableView = tableView;
     }
}
