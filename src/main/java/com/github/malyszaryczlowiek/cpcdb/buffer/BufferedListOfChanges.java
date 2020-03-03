package com.github.malyszaryczlowiek.cpcdb.buffer;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;

import java.util.*;

public class BufferedListOfChanges implements BufferState
{
    private static List<Set<Change>> listOfSetOfChanges = null;
    private static int currentIndexOfChanges = 0;

    private BufferedListOfChanges() {}

    public static BufferedListOfChanges getBufferedListOfChanges() {
        if (listOfSetOfChanges == null)
            listOfSetOfChanges = new ArrayList<>();
        return new BufferedListOfChanges();
    }

    @Override
    public synchronized <T> void addChange(ActionType actionType, Map<Integer,Compound> mapOfCompounds, Field field, T newValue) {
        int sizeOfListOfChanges = listOfSetOfChanges.size();
        if (sizeOfListOfChanges > currentIndexOfChanges)
            listOfSetOfChanges.removeAll( listOfSetOfChanges.subList( currentIndexOfChanges, sizeOfListOfChanges));
        Set<Change> setOfChanges = new HashSet<>();
        setOfChanges.add(new Change(actionType, mapOfCompounds, field, newValue));
        listOfSetOfChanges.add(setOfChanges);
        if (actionType.equals(ActionType.EDIT)) ((Change) setOfChanges.toArray()[0]).swipeModificationDate();
        currentIndexOfChanges = listOfSetOfChanges.size();
    }

    @Override
    public void addSetOfChanges(Set<Change> setOfChanges) {
        int sizeOfListOfChanges = listOfSetOfChanges.size();
        if (sizeOfListOfChanges > currentIndexOfChanges)
            listOfSetOfChanges.removeAll( listOfSetOfChanges.subList( currentIndexOfChanges, sizeOfListOfChanges));
        listOfSetOfChanges.add(setOfChanges);
        ((Change) setOfChanges.toArray()[0]).swipeModificationDate();
        currentIndexOfChanges = listOfSetOfChanges.size();
    }

    public synchronized Map<Integer,Compound> undo() throws IndexOutOfBoundsException {
        if (currentIndexOfChanges > 0) {
            --currentIndexOfChanges;
            listOfSetOfChanges.get(currentIndexOfChanges).forEach( Change::swipeValues );
            ((Change) listOfSetOfChanges.get(currentIndexOfChanges).toArray()[0]).swipeModificationDate();
            return listOfSetOfChanges.get(currentIndexOfChanges).stream().iterator().next().getMapOfCompounds();
        }
        else throw new IndexOutOfBoundsException("INDEX WYJECHAŁ PONIŻEJ 1");
    }

    public synchronized Map<Integer,Compound> redo() throws IndexOutOfBoundsException {
        if ( listOfSetOfChanges.size() > currentIndexOfChanges ) {
            listOfSetOfChanges.get(currentIndexOfChanges).forEach( Change::swipeValues );
            ((Change) listOfSetOfChanges.get(currentIndexOfChanges).toArray()[0]).swipeModificationDate();
            Map<Integer, Compound> mapToReturn = listOfSetOfChanges.get(currentIndexOfChanges).stream()
                    .iterator().next().getMapOfCompounds();
            ++currentIndexOfChanges;
            return mapToReturn;
        }
        else throw new IndexOutOfBoundsException("INDEX JEST USTAWIONY NA SIZE()");
    }

    public synchronized ActionType getActionTypeOfCurrentOperation() throws IndexOutOfBoundsException {
        if (currentIndexOfChanges >= 0)
            return listOfSetOfChanges.get(currentIndexOfChanges).stream().iterator().next().getActionType();
        else throw new IndexOutOfBoundsException("INDEX WYJECHAŁ PONIŻEJ 1");
    }

    @Override
    public synchronized int returnCurrentIndex() { return currentIndexOfChanges; }

    @Override
    public int getBufferSize() { return listOfSetOfChanges.size(); }
}
