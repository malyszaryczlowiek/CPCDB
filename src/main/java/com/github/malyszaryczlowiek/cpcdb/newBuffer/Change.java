package com.github.malyszaryczlowiek.cpcdb.newBuffer;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;
import com.github.malyszaryczlowiek.cpcdb.compound.TempStability;
import com.github.malyszaryczlowiek.cpcdb.compound.Unit;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public class Change
{
    private ActionType actionType;
    private Map<Integer,Compound> mapOfCompounds;
    private Field field;
    private String newString;
    private Float newFloat;
    private LocalDateTime newModificationTime;
    private Boolean newBoolean;

    <T> Change(ActionType actionType, Map<Integer,Compound> mapOfCompounds, Field field, T newValue) throws IllegalArgumentException {
        this.actionType = actionType;
        this.mapOfCompounds = mapOfCompounds;
        this.field = field;
        this.newModificationTime = LocalDateTime.now();
        // jeśli typem operacji jest zmiana to musimy widziec jakiego typu jest nowa wartość
        if ( actionType.equals(ActionType.EDIT)) {
            if ( newValue instanceof String && (
                    field.equals(Field.SMILES) ||
                    field.equals(Field.COMPOUNDNUMBER) ||
                    field.equals(Field.UNIT) ||
                    field.equals(Field.FORM) ||
                    field.equals(Field.CONTAINER) ||
                    field.equals(Field.TEMPSTABILITY) ||
                    field.equals(Field.STORAGEPLACE) ||
                    field.equals(Field.ADDITIONALINFO) )
            )
                newString = (String) newValue;
            else if ( newValue instanceof Float && field.equals(Field.AMOUNT)) newFloat = (Float) newValue;
            else if ( newValue instanceof Boolean && field.equals(Field.ARGON)) newBoolean = (Boolean) newValue;
            else throw new IllegalArgumentException("Value type and Field are not consistent when ActionType.EDIT.");
        }
        swipeValues();
    }

    void swipeValues() {
        try {
            if ( actionType.equals(ActionType.EDIT) ) {
                Compound compound = (Compound) mapOfCompounds.values().toArray()[0];
                String swipeString;
                float swipeFloat;
                boolean swipeBoolean;
                if ( field == Field.SMILES ) {
                    swipeString = compound.getSmiles();
                    compound.setSmiles(newString);
                    newString = swipeString;
                }
                else if ( field == Field.COMPOUNDNUMBER ) {
                    swipeString = compound.getCompoundNumber();
                    compound.setCompoundNumber(newString);
                    newString = swipeString;
                }
                else if ( field == Field.AMOUNT ) {
                    swipeFloat = compound.getAmount();
                    compound.setAmount(newFloat);
                    newFloat = swipeFloat;
                }
                else if ( field == Field.UNIT ) {
                    swipeString = compound.getUnit().toString();
                    compound.setUnit( Unit.stringToEnum(newString) );
                    newString = swipeString;
                }
                else if ( field == Field.FORM ) {
                    swipeString = compound.getForm();
                    compound.setForm(newString);
                    newString = swipeString;
                }
                else if ( field == Field.CONTAINER ) {
                    swipeString = compound.getContainer();
                    compound.setContainer(newString);
                    newString = swipeString;
                }
                else if ( field == Field.TEMPSTABILITY ) {
                    swipeString = compound.getTempStability().toString();
                    compound.setTempStability( TempStability.stringToEnum(newString) );
                    newString = swipeString;
                }
                else if ( field == Field.ARGON ) {
                    swipeBoolean = compound.isArgon();
                    compound.setArgon(newBoolean);
                    newBoolean = swipeBoolean;
                }
                else if ( field == Field.STORAGEPLACE ) {
                    swipeString = compound.getStoragePlace();
                    compound.setStoragePlace(newString);
                    newString = swipeString;
                }
                else if ( field == Field.ADDITIONALINFO ) {
                    swipeString = compound.getAdditionalInfo();
                    compound.setAdditionalInfo(newString);
                    newString = swipeString;
                }
                else
                    throw new IOException("Value type and Field are not consistent when ActionType.EDIT.");
                LocalDateTime oldModificationTime = compound.getDateTimeModification();
                compound.setDateTimeModification(newModificationTime);
                newModificationTime = oldModificationTime;
            }
            if (actionType.equals( ActionType.REMOVE )) {
                mapOfCompounds.forEach( (index, compound1) -> {
                    if (compound1.isToDelete()) compound1.setToDelete(false);
                    else compound1.setToDelete(true);
                });
            }
            if (actionType.equals( ActionType.INSERT )) {
                mapOfCompounds.forEach( (index, compound1) -> {
                    if (compound1.isSavedInDatabase()) compound1.setSavedInDatabase(false);
                    else compound1.setSavedInDatabase(true);
                });
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public ActionType getActionType() { return actionType; }

    public Map<Integer, Compound> getMapOfCompounds() { return mapOfCompounds; }

    public Field getField() { return field; }
}
