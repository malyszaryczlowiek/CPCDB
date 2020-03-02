package com.github.malyszaryczlowiek.cpcdb.newBuffer;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BufferedListOfChanges implements BufferState
{
    private static List<Change> listOfChanges = null;
    private static int currentIndexOfChanges = 0;

    private BufferedListOfChanges() {}

    public static BufferedListOfChanges getBufferedListOfChanges() {
        if (listOfChanges == null )
            listOfChanges = new ArrayList<>();
        return new BufferedListOfChanges();
    }

    @Override
    public synchronized <T> void addChange(ActionType actionType, Map<Integer,Compound> mapOfCompounds, Field field, T newValue) {
        int sizeOfListOfChanges = listOfChanges.size();
        if (sizeOfListOfChanges > currentIndexOfChanges)
            listOfChanges.removeAll( listOfChanges.subList( currentIndexOfChanges, sizeOfListOfChanges));
        listOfChanges.add(new Change(actionType, mapOfCompounds, field, newValue));
        currentIndexOfChanges = listOfChanges.size();
    }

    public synchronized Map<Integer,Compound> undo() throws IndexOutOfBoundsException {
        if (currentIndexOfChanges > 0) {
            --currentIndexOfChanges;
            listOfChanges.get(currentIndexOfChanges).swipeValues();
            return listOfChanges.get(currentIndexOfChanges).getMapOfCompounds();
        }
        else throw new IndexOutOfBoundsException("INDEX WYJECHAŁ PONIŻEJ 1");
    }

    public synchronized Map<Integer,Compound> redo() throws IndexOutOfBoundsException {
        if ( listOfChanges.size() > currentIndexOfChanges ) {
            listOfChanges.get(currentIndexOfChanges).swipeValues();
            Map<Integer, Compound> mapToReturn = listOfChanges.get(currentIndexOfChanges).getMapOfCompounds();
            ++currentIndexOfChanges;
            return mapToReturn;
        }
        else throw new IndexOutOfBoundsException("INDEX JEST USTAWIONY NA SIZE()");
    }

    public synchronized ActionType getActionTypeOfCurrentOperation() throws IndexOutOfBoundsException {
        if (currentIndexOfChanges >= 0) return listOfChanges.get(currentIndexOfChanges).getActionType();
        else throw new IndexOutOfBoundsException("INDEX WYJECHAŁ PONIŻEJ 1");
    }

    @Override
    public synchronized int returnCurrentIndex() { return currentIndexOfChanges; }

    @Override
    public int getBufferSize() { return listOfChanges.size(); }
}
