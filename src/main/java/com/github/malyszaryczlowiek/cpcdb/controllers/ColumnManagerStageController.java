package com.github.malyszaryczlowiek.cpcdb.controllers;

import com.github.malyszaryczlowiek.cpcdb.managers.ColumnManager;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;


public class ColumnManagerStageController implements Initializable
{
    @FXML private ToggleButton selectAll;
    @FXML private ToggleButton smiles;
    @FXML private ToggleButton compoundNum;
    @FXML private ToggleButton amount;
    @FXML private ToggleButton unit;
    @FXML private ToggleButton form;
    @FXML private ToggleButton tempStability;
    @FXML private ToggleButton argon;
    @FXML private ToggleButton container;
    @FXML private ToggleButton storagePlace;
    @FXML private ToggleButton lastModification;
    @FXML private ToggleButton additionalInfo;

    private Stage stage;
    private Map<ToggleButton, Field> mapOfButtons;
    private Map<Field, ToggleButton> mapOfButtonsReversed;
    private List<Field> listOfRecentlyVisibleColumns;
    private ColumnManager columnManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ToggleButton[] toggleButtons = {smiles, compoundNum, amount, unit,
                form, tempStability, argon, container,
                storagePlace, lastModification, additionalInfo};

        Field[] fields = Field.values();
        columnManager = new ColumnManager();
        mapOfButtons = new HashMap<>(11);
        mapOfButtonsReversed = new HashMap<>(11);
        listOfRecentlyVisibleColumns = new ArrayList<>(11);
        for (int i = 0; i < toggleButtons.length; ++i ){
            boolean visibleAndSelected = columnManager.isColumnVisible(fields[i]);
            mapOfButtons.putIfAbsent(toggleButtons[i], fields[i]);
            mapOfButtonsReversed.putIfAbsent(fields[i], toggleButtons[i]);
            if (visibleAndSelected)
                listOfRecentlyVisibleColumns.add(fields[i]);
            toggleButtons[i].setSelected(visibleAndSelected);
        }
        selectAll.setSelected( columnManager.areAllColumnsVisible() );
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    protected void onSelectedColumnClicked(ActionEvent event) {
        Field field = retrieveFieldFromEvent(event);
        boolean visible = retrieveButtonFromEvent(event).isSelected();
        columnManager.onShowHideColumn(field,visible);
        if (listOfRecentlyVisibleColumns.contains(field))
            listOfRecentlyVisibleColumns.remove(field);
        else
            listOfRecentlyVisibleColumns.add(field);
        selectAll.setSelected( columnManager.areAllColumnsVisible() );
        event.consume();
    }

    @FXML
    protected void onSelectAllClicked() {
        if ( selectAll.isSelected() ) {
            columnManager.onShowHideAllColumns();
            mapOfButtons.keySet().forEach(key -> key.setSelected(true));
            mapOfButtonsReversed.values().forEach(toggleButton -> toggleButton.setSelected(true));
        }
        else {
            mapOfButtonsReversed.keySet().forEach(
                    field -> {
                        boolean visibility = listOfRecentlyVisibleColumns.contains(field);
                        mapOfButtonsReversed.get(field).setSelected(visibility);
                        columnManager.onShowHideColumn( field, visibility);
                    }
            );
        }
    }

    @FXML
    protected void onCloseButtonClicked() {
        stage.close();
    }

    private Field retrieveFieldFromEvent(ActionEvent event) {
        return mapOfButtons.keySet()
                .stream()
                .filter( key -> key.equals(event.getSource()) )
                .map(button -> mapOfButtons.get(button))
                .collect( Collectors.toList() )
                .get(0);
    }

    private ToggleButton retrieveButtonFromEvent(ActionEvent event) {
        return mapOfButtons.keySet()
                .stream()
                .filter( key -> key.equals(event.getSource()) )
                .collect( Collectors.toList() )
                .get(0);
    }
}


