package com.github.malyszaryczlowiek.cpcdb.managers;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;
import com.github.malyszaryczlowiek.cpcdb.managers.initializers.*;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ColumnManager
{
    private static Map<Field, TableColumn<Compound,String>> mapOfColumnsString;
    private static Map<Field, TableColumn<Compound,Boolean>> mapOfColumnsBoolean;
    private static Map<Field, TableColumn<Compound, LocalDateTime>> mapOfColumnsLocalDateTime;

    private static MenuItem menuShowAll;
    private static MenuItem contextMenuShowAll;

    public ColumnManager () {}

    public ColumnManager ( MenuItem[] menuShowAllTable,
                           TableColumn<Compound,String> smilesCol,
                           TableColumn<Compound,String> compoundNumCol,
                           TableColumn<Compound,String> amountCol,
                           TableColumn<Compound,String> unitCol,
                           TableColumn<Compound,String> formCol,
                           TableColumn<Compound,String> tempStabilityCol,
                           TableColumn<Compound,Boolean> argonCol,
                           TableColumn<Compound,String> containerCol,
                           TableColumn<Compound,String> storagePlaceCol,
                           TableColumn<Compound,LocalDateTime>  lastModificationCol,
                           TableColumn<Compound,String> additionalInfoCol) {
        menuShowAll = menuShowAllTable[0];
        contextMenuShowAll = menuShowAllTable[1];

        mapOfColumnsString = new HashMap<>(9);
        mapOfColumnsBoolean = new HashMap<>(1);
        mapOfColumnsLocalDateTime = new HashMap<>(1);

        mapOfColumnsString.putIfAbsent(Field.SMILES, smilesCol);
        mapOfColumnsString.putIfAbsent(Field.COMPOUNDNUMBER, compoundNumCol);
        mapOfColumnsString.putIfAbsent(Field.AMOUNT, amountCol);
        mapOfColumnsString.putIfAbsent(Field.UNIT, unitCol);
        mapOfColumnsString.putIfAbsent(Field.FORM, formCol);
        mapOfColumnsString.putIfAbsent(Field.TEMPSTABILITY, tempStabilityCol);
        mapOfColumnsString.putIfAbsent(Field.CONTAINER, containerCol);
        mapOfColumnsString.putIfAbsent(Field.STORAGEPLACE, storagePlaceCol);
        mapOfColumnsString.putIfAbsent(Field.ADDITIONALINFO, additionalInfoCol);

        mapOfColumnsBoolean.putIfAbsent(Field.ARGON, argonCol);

        mapOfColumnsLocalDateTime.putIfAbsent(Field.DATETIMEMODIFICATION, lastModificationCol);
    }

    public void onShowHideColumn( Field field, boolean visibility ) {
        switch (field) {
            case ARGON:
                mapOfColumnsBoolean.get(Field.ARGON).setVisible(visibility);
                break;
            case DATETIMEMODIFICATION:
                mapOfColumnsLocalDateTime.get(Field.DATETIMEMODIFICATION).setVisible(visibility);
                break;
            default:
                mapOfColumnsString.get(field).setVisible(visibility);
                break;
        }

        setPropertyForFieldTo(field, Boolean.toString(visibility));
        if ( areAllColumnsVisible() ) {
            menuShowAll.setDisable(true);
            contextMenuShowAll.setDisable(true);
        }
        else {
            menuShowAll.setDisable(false);
            contextMenuShowAll.setDisable(false);
        }
    }

    public boolean areAllColumnsVisible() {
        return mapOfColumnsString.values().stream().allMatch( TableColumnBase::isVisible )
                &&  mapOfColumnsBoolean.values().stream().allMatch( TableColumnBase::isVisible )
                && mapOfColumnsLocalDateTime.values().stream().allMatch( TableColumnBase::isVisible );
    }

    public void onShowHideAllColumns() {
        mapOfColumnsString.values().forEach(compoundTableColumn -> compoundTableColumn.setVisible(true));
        mapOfColumnsBoolean.values().forEach(compoundTableColumn -> compoundTableColumn.setVisible(true));
        mapOfColumnsLocalDateTime.values().forEach(compoundTableColumn -> compoundTableColumn.setVisible(true));
        setAllColumnVisibilityToTrue();
        menuShowAll.setDisable(true);
        contextMenuShowAll.setDisable(true);
    }

    public void setColumnFontSize() {
        String fontSize = SecureProperties.getProperty("settings.fontSize");
        mapOfColumnsString.values()
                .forEach( compoundTableColumn ->
                        compoundTableColumn.getStyleClass().add( fontSize )
                );
        mapOfColumnsBoolean.values()
                .forEach( compoundTableColumn ->
                        compoundTableColumn.getStyleClass().add( fontSize )
                );
        mapOfColumnsLocalDateTime.values()
                .forEach( compoundTableColumn ->
                        compoundTableColumn.getStyleClass().add( fontSize )
                );
    }

    public boolean isColumnVisible(Field field) {
        switch (field) {
            case ARGON:
                return mapOfColumnsBoolean.get(field).isVisible();
            case DATETIMEMODIFICATION:
                return mapOfColumnsLocalDateTime.get(field).isVisible();
            default:
                return mapOfColumnsString.get(field).isVisible();
        }
    }

    public void changeFontSize(String[] oldValue, String newValue) {
        mapOfColumnsString.values().forEach( compoundStringTableColumn -> {
            ObservableList<String> styleClass = compoundStringTableColumn.getStyleClass();
            styleClass.removeAll(oldValue);
            styleClass.add(newValue);
        });
        mapOfColumnsLocalDateTime.values().forEach( compoundStringTableColumn -> {
            ObservableList<String> styleClass = compoundStringTableColumn.getStyleClass();
            styleClass.removeAll(oldValue);
            styleClass.add(newValue);
        });
        mapOfColumnsBoolean.values().forEach( compoundStringTableColumn -> {
            ObservableList<String> styleClass = compoundStringTableColumn.getStyleClass();
            styleClass.removeAll(oldValue);
            styleClass.add(newValue);
        });
    }

    public void initializeColumns(Field... fields) {
        for (Field field: fields) {
            switch (field) {
                case SMILES:
                    new SmilesColumnInitializer( mapOfColumnsString.get(Field.SMILES) ).initialize(); break;
                case COMPOUNDNUMBER:
                    new CompoundNumberInitializer( mapOfColumnsString.get(Field.COMPOUNDNUMBER) ).initialize(); break;
                case AMOUNT:
                    new AmountColumnInitializer( mapOfColumnsString.get(Field.AMOUNT) ).initialize(); break;
                case UNIT:
                    new UnitColumnInitializer( mapOfColumnsString.get(Field.UNIT) ).initialize(); break;
                case FORM:
                    new FormColumnInitializer( mapOfColumnsString.get(Field.FORM) ).initialize(); break;
                case TEMPSTABILITY:
                    new TemperatureStabilityColumnInitializer( mapOfColumnsString.get(Field.TEMPSTABILITY) ).initialize(); break;
                case ARGON:
                    new ArgonColumnInitializer( mapOfColumnsBoolean.get(Field.ARGON) ).initialize(); break;
                case CONTAINER:
                    new ContainerColumnInitializer( mapOfColumnsString.get(Field.CONTAINER) ).initialize(); break;
                case STORAGEPLACE:
                    new StoragePlaceColumnInitializer( mapOfColumnsString.get(Field.STORAGEPLACE) ).initialize(); break;
                case DATETIMEMODIFICATION:
                    new LastModificationColumnInitializer( mapOfColumnsLocalDateTime.get(Field.DATETIMEMODIFICATION)).initialize(); break;
                case ADDITIONALINFO:
                    new AdditionalInfoColumnInitializer( mapOfColumnsString.get(Field.ADDITIONALINFO) ).initialize(); break;
                default:
                    break;
            }
        }
    }

    public void setAllColumnsWidthToProperties() {
        SecureProperties.setProperty("column.width.Smiles", String.valueOf( mapOfColumnsString.get(Field.SMILES).getWidth() ));
        SecureProperties.setProperty("column.width.CompoundName", String.valueOf( mapOfColumnsString.get(Field.COMPOUNDNUMBER).getWidth() )); // TODO zmienić nazwę propertiesa z compoundname na compoundNumber
        SecureProperties.setProperty("column.width.Amount", String.valueOf( mapOfColumnsString.get(Field.AMOUNT).getWidth() ));
        SecureProperties.setProperty("column.width.Unit", String.valueOf( mapOfColumnsString.get(Field.UNIT).getWidth() ));
        SecureProperties.setProperty("column.width.Form", String.valueOf( mapOfColumnsString.get(Field.FORM).getWidth() ));
        SecureProperties.setProperty("column.width.TemperatureStability", String.valueOf( mapOfColumnsString.get(Field.TEMPSTABILITY).getWidth() ));
        SecureProperties.setProperty("column.width.Argon", String.valueOf( mapOfColumnsBoolean.get(Field.ARGON).getWidth() ));
        SecureProperties.setProperty("column.width.Container", String.valueOf( mapOfColumnsString.get(Field.CONTAINER).getWidth() ));
        SecureProperties.setProperty("column.width.StoragePlace", String.valueOf( mapOfColumnsString.get(Field.STORAGEPLACE).getWidth() ));
        SecureProperties.setProperty("column.width.LastModification", String.valueOf( mapOfColumnsLocalDateTime.get(Field.DATETIMEMODIFICATION).getWidth() ));
        SecureProperties.setProperty("column.width.AdditionalInfo", String.valueOf( mapOfColumnsString.get(Field.ADDITIONALINFO).getWidth() ));
    }

    private void setAllColumnVisibilityToTrue() {
        SecureProperties.setProperty("column.show.Smiles", "true");
        SecureProperties.setProperty("column.show.CompoundName", "true");
        SecureProperties.setProperty("column.show.Amount", "true");
        SecureProperties.setProperty("column.show.Unit", "true");
        SecureProperties.setProperty("column.show.Form", "true");
        SecureProperties.setProperty("column.show.TemperatureStability", "true");
        SecureProperties.setProperty("column.show.Argon", "true");
        SecureProperties.setProperty("column.show.Container", "true");
        SecureProperties.setProperty("column.show.StoragePlace", "true");
        SecureProperties.setProperty("column.show.LastModification", "true");
        SecureProperties.setProperty("column.show.AdditionalInfo", "true");
    }

    private void setPropertyForFieldTo(Field field, String bool) {
        switch (field) {
            case SMILES:
                SecureProperties.setProperty("column.show.Smiles", bool);
                break;
            case COMPOUNDNUMBER:
                SecureProperties.setProperty("column.show.CompoundName", bool);
                break;
            case AMOUNT:
                SecureProperties.setProperty("column.show.Amount", bool);
                break;
            case UNIT:
                SecureProperties.setProperty("column.show.Unit", bool);
                break;
            case  FORM:
                SecureProperties.setProperty("column.show.Form", bool);
                break;
            case TEMPSTABILITY:
                SecureProperties.setProperty("column.show.TemperatureStability", bool);
                break;
            case ARGON:
                SecureProperties.setProperty("column.show.Argon", bool);
                break;
            case CONTAINER:
                SecureProperties.setProperty("column.show.Container", bool);
                break;
            case STORAGEPLACE:
                SecureProperties.setProperty("column.show.StoragePlace", bool);
                break;
            case DATETIMEMODIFICATION:
                SecureProperties.setProperty("column.show.LastModification", bool);
                break;
            case ADDITIONALINFO:
                SecureProperties.setProperty("column.show.AdditionalInfo", bool);
                break;
            default:
                break;
        }
    }
}























