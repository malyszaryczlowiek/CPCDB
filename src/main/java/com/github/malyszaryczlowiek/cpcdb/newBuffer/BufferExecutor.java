package com.github.malyszaryczlowiek.cpcdb.newBuffer;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;

import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;

import java.util.Map;

public class BufferExecutor implements Buffer, BufferState
{
    private static TableView<Compound> mainSceneTableView;
    private static ObservableList<Compound> observableList;
    private static MenuItem menuEditUndo;
    private static MenuItem menuEditRedo;
    private static MenuItem menuFileSave;
    private static BufferedListOfChanges bufferedListOfChanges;

    /*
    public static BufferExecutor getBufferExecutor() throws IllegalArgumentException {
        if (bufferedListOfChanges == null)
            throw new IllegalArgumentException("You should set TableView<Compound> and " +
                    "ObservableList<Compound> arguments first");
        return new BufferExecutor();
    }
     */

    public static BufferExecutor getBufferExecutor( TableView<Compound> tableView,
                                                    ObservableList<Compound> observableListOfCompounds,
                                                    MenuItem undo, MenuItem redo, MenuItem fileSave) {
        mainSceneTableView = tableView;
        observableList = observableListOfCompounds;
        menuEditUndo = undo;
        menuEditRedo = redo;
        menuFileSave = fileSave;
        bufferedListOfChanges = BufferedListOfChanges.getBufferedListOfChanges();
        return new BufferExecutor();
    }

    @Override
    public <T> void addChange(ActionType actionType, Map<Integer, Compound> mapOfCompounds, Field field, T newValue) {
        bufferedListOfChanges.addChange(actionType,mapOfCompounds,field,newValue);
        if (actionType.equals(ActionType.REMOVE)) observableList.removeAll(mapOfCompounds.values());
        checkUndoRedoMenuItems();
    }

    @Override
    public void undo() { if (bufferedListOfChanges.returnCurrentIndex() > 0) executeUndoOrRedo(true); }

    @Override
    public void redo() {
        if (bufferedListOfChanges.getBufferSize() > bufferedListOfChanges.returnCurrentIndex()) executeUndoOrRedo(false);
    }

    private void executeUndoOrRedo(boolean executeUndo) {
        ActionType actionType;
        Map<Integer, Compound> mapOfCompoundsToChangeInTableView;
        if (executeUndo) {
            mapOfCompoundsToChangeInTableView = bufferedListOfChanges.undo();
            actionType = bufferedListOfChanges.getActionTypeOfCurrentOperation();
        }
        else {
            actionType = bufferedListOfChanges.getActionTypeOfCurrentOperation();
            mapOfCompoundsToChangeInTableView = bufferedListOfChanges.redo();
        }
        if ( actionType.equals(ActionType.REMOVE) ) { // TUTaj trzeba sprawdzić czy nie leży problem z insertemn
            boolean arAllToDelete = mapOfCompoundsToChangeInTableView.values().stream().allMatch( Compound::isToDelete ); // TODO to może powodować problemy sprawdzić w metodzie swipe czy wartość
            if ( arAllToDelete ) observableList.removeAll( mapOfCompoundsToChangeInTableView.values() ); // id to delete odnosi się tylko do remove nie odnosi dię do insertu.
            else mapOfCompoundsToChangeInTableView.forEach( (index, compound) -> observableList.add(index, compound) );
        }
        if (actionType.equals(ActionType.INSERT)) {
            Compound compound = (Compound) mapOfCompoundsToChangeInTableView.values().toArray()[0];
            boolean savedInDatabase = compound.isSavedInDatabase();
            if (savedInDatabase) observableList.remove(compound);
            else mapOfCompoundsToChangeInTableView.forEach( (index, comp) -> observableList.add(index, comp) );
        }
        checkUndoRedoMenuItems();
    }

    public void saveChangesToDatabase() {

    }

    private void checkUndoRedoMenuItems() {
        mainSceneTableView.refresh();
        if (bufferedListOfChanges.getBufferSize() == 0) {
            menuEditUndo.setDisable(true);
            menuEditRedo.setDisable(true);
        }
        if (bufferedListOfChanges.getBufferSize() > 0 && bufferedListOfChanges.returnCurrentIndex() == 0) {
            menuEditUndo.setDisable(true);
            menuEditRedo.setDisable(false);
        }
        if (bufferedListOfChanges.getBufferSize() > bufferedListOfChanges.returnCurrentIndex()
                && bufferedListOfChanges.returnCurrentIndex() > 0) {
            menuEditUndo.setDisable(false);
            menuEditRedo.setDisable(false);
        }
        if ( bufferedListOfChanges.getBufferSize() == bufferedListOfChanges.returnCurrentIndex()
                && bufferedListOfChanges.getBufferSize() > 0) {
            menuEditUndo.setDisable(false);
            menuEditRedo.setDisable(true);
        }
        if (bufferedListOfChanges.returnCurrentIndex() > 0) menuFileSave.setDisable(false);
        else menuFileSave.setDisable(true);
    }

    @Override
    public int getBufferSize() { return bufferedListOfChanges.getBufferSize(); }

    @Override
    public int returnCurrentIndex() { return bufferedListOfChanges.returnCurrentIndex(); }
}
