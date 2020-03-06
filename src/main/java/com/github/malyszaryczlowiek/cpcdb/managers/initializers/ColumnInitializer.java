package com.github.malyszaryczlowiek.cpcdb.managers.initializers;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;
import com.github.malyszaryczlowiek.cpcdb.buffer.ActionType;
import com.github.malyszaryczlowiek.cpcdb.buffer.BufferExecutor;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.util.Map;
import java.util.TreeMap;

public class ColumnInitializer
{
     protected static BufferExecutor bufferExecutor;
     protected static TableView<Compound> mainSceneTableView;
     protected static ObservableList<Compound> observableList;

     public static void setUpInitializer(TableView<Compound> tableView, BufferExecutor bufferExecutor1, ObservableList<Compound> observableList1) {
          bufferExecutor = bufferExecutor1;
          mainSceneTableView = tableView;
          observableList = observableList1;
     }

      protected <T> void saveChangeToBufferExecutor(Compound compound, Field field, T newValue) {
           int index = observableList.indexOf(compound);
           Map<Integer, Compound> editedCompound = new TreeMap<>();
           editedCompound.put(index, compound);
           bufferExecutor.addChange(ActionType.EDIT, editedCompound, field, newValue);
      }
}
