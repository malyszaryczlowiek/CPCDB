package com.github.malyszaryczlowiek.cpcdb.buffer;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class collects every single change for each
 * compound and notify compound what will be changed.
 */
class CollectorOfChanges
{
    private static List<Compound> listOfCompoundsToDeleteFromDB;
    private static List<Compound> listOfCompoundsToInsertInDB;
    private static List<Compound> listOfCompoundsToEditInDB;

    /**
     * function which collect informations of changes for each compound
     * and
     * @param list list of executed changes
     */
    static void collect(List<CompoundChange> list)
    {
        // wyłuskaj compound
        // do jego listy zmian dodaj field to change, który przechowuje
        // zrób stream niepowtarzających się compoundów i zwróć go w postaci listy na końcu funkcji

        // TODO to można zrobić w wielu wątkach bo mamy tylko do odczytu

        // jak będziemy mieli compoundy do usunięcia to możemy je potem odjąć od tych
        // które są do zmiany i do wstawienia.
        listOfCompoundsToDeleteFromDB = list.stream()
                .filter(compoundChange -> compoundChange.getActionType().equals(ActionType.REMOVE))
                .flatMap( CollectorOfChanges::convertToStream )
                // compoundChange -> convertToStream(compoundChange)
                .filter( Compound::isSavedInDatabase )
                .distinct()
                .collect(Collectors.toList()); // tutaj są te które są do usunięcia i są zapisane w bazie danych

        List<Compound> listOfCompoundsToDeleteFromInsertList = list.stream()
                .filter(compoundChange -> compoundChange.getActionType().equals(ActionType.REMOVE))
                .flatMap( CollectorOfChanges::convertToStream )
                // compoundChange -> convertToStream(compoundChange)
                .filter(compound-> !compound.isSavedInDatabase())
                .distinct()
                .collect(Collectors.toList());// tutaj są te które są do usunięcia i nie są zapisane w bazie danych
        // trzeba je odjąć od tych do dodania do bazy


        listOfCompoundsToInsertInDB = list.stream()
                .filter(compoundChange -> compoundChange.getActionType().equals(ActionType.INSERT))
                .flatMap( CollectorOfChanges::convertToStream )
                // compoundChange -> convertToStream(compoundChange)
                //.filter(compound -> !compound.isSavedInDatabase())
                .distinct()
                .collect(Collectors.toList());

        listOfCompoundsToInsertInDB.removeAll(listOfCompoundsToDeleteFromInsertList);

        listOfCompoundsToEditInDB = list.stream()
                .filter(compoundChange -> compoundChange.getActionType().equals(ActionType.EDIT))
                .filter(compoundChange -> compoundChange.getCompound().isSavedInDatabase())
                .filter(compoundChange -> !compoundChange.getCompound().isToDelete())
                .map(compoundChange ->
                {
                    Compound compound = compoundChange.getCompound();
                    Field fieldToChange = compoundChange.getField();
                    compound.setFieldToChange(fieldToChange);
                    return compound;
                })
                .distinct()
                .collect(Collectors.toList());
    }

    static List<Compound> getListOfCompoundsToDeleteFromDB()
    {
        return listOfCompoundsToDeleteFromDB;
    }

    static List<Compound> getListOfCompoundsToInsert()
    {
        return listOfCompoundsToInsertInDB;
    }

    static List<Compound> getListOfCompoundsToEditInDB()
    {
        return listOfCompoundsToEditInDB;
    }

    static void clearAllLists()
    {
        listOfCompoundsToDeleteFromDB.clear();
        listOfCompoundsToInsertInDB.clear();
        listOfCompoundsToEditInDB.clear();
    }

    private static Stream<Compound> convertToStream(CompoundChange compoundChange)
    {
        return compoundChange.getMapOfCompounds().values().stream();
    }

    // zrobić w kolejności usówanie, edytowanie ,insertowanie
}
