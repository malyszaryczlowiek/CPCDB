package com.github.malyszaryczlowiek.cpcdb.buffer;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;

import com.github.malyszaryczlowiek.cpcdb.db.SaveChangesToLocalDatabase;
import com.github.malyszaryczlowiek.cpcdb.tasks.SaveChangesToRemoteDatabase;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;

import java.util.Map;
import java.util.Set;

public class BufferExecutor implements Buffer, BufferState
{
    private static TableView<Compound> mainSceneTableView;
    private static ObservableList<Compound> observableList;
    private static MenuItem menuEditUndo;
    private static MenuItem menuEditRedo;
    private static MenuItem menuFileSave;
    private static BufferedListOfChanges bufferedListOfChanges;

    public static BufferExecutor getBufferExecutor() throws IllegalArgumentException {
        if (bufferedListOfChanges == null)
            throw new IllegalArgumentException("You should set TableView<Compound> and " +
                    "ObservableList<Compound> arguments first");
        return new BufferExecutor();
    }

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
    public void addSetOfChanges(Set<Change> changeSet) {
        bufferedListOfChanges.addSetOfChanges(changeSet);
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
        if ( actionType.equals(ActionType.REMOVE) ) {
            boolean arAllToDelete = mapOfCompoundsToChangeInTableView.values().stream().allMatch( Compound::isToDelete );
            if ( arAllToDelete ) observableList.removeAll( mapOfCompoundsToChangeInTableView.values() );
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

    public void saveChangesToDatabase(boolean startAsDemon) { // TODO tutaj aktualnie jestem Implementuje taski;
        Task<Void> savingToRemoteDatabaseTask = SaveChangesToRemoteDatabase.getTask();
        Task<Void> savingToLocalDatabaseTask = SaveChangesToLocalDatabase.getTask();
        Thread savingToRemoteDatabaseThread = new Thread(savingToRemoteDatabaseTask);
        Thread savingToLocalDatabaseThread = new Thread(savingToLocalDatabaseTask);
        if (startAsDemon) {
            savingToRemoteDatabaseThread.setDaemon(true);
            savingToLocalDatabaseThread.setDaemon(true);
        }
        savingToRemoteDatabaseThread.start();
        savingToLocalDatabaseThread.start();
        if (!startAsDemon) {
            try {
                savingToRemoteDatabaseThread.join();
                savingToLocalDatabaseThread.join();
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
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

    public int getIndexOfCompound(Compound compound) { return observableList.indexOf(compound); }
}
