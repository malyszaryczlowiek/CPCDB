package com.github.malyszaryczlowiek.cpcdb.controllers;

import com.github.malyszaryczlowiek.cpcdb.compound.*;
import com.github.malyszaryczlowiek.cpcdb.buffer.ActionType;
import com.github.malyszaryczlowiek.cpcdb.buffer.BufferExecutor;
import com.github.malyszaryczlowiek.cpcdb.buffer.Change;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ErrorType;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ShortAlertWindowFactory;
import com.github.malyszaryczlowiek.cpcdb.windows.windowLoaders.StageManager;
import com.github.malyszaryczlowiek.cpcdb.windows.windowLoaders.WindowsEnum;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

public class EditCompoundStageController implements Initializable
{
    private Stage stage;
    private StageManager stageManager;
    private Compound compound;

    @FXML private TextField smilesShowEdit;
    @FXML private TextField compoundNumberShowEdit;
    @FXML private TextField amountShowEdit;

    @FXML private TextArea formShowEdit;
    @FXML private TextArea containerShowEdit;
    @FXML private TextArea storagePlaceShowEdit;
    @FXML private TextArea additionalInfoShowEdit;

    @FXML private ChoiceBox<String> unitChoiceBox;
    @FXML private ChoiceBox<String> tempStabilityChoiceBox;

    @FXML private CheckBox argonCheckBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> unitsObsList = FXCollections.observableList( Unit.returnValues() );
        unitChoiceBox.setItems(unitsObsList);
        ObservableList<String> tempObsList = FXCollections.observableList( TempStability.returnValues() );
        tempStabilityChoiceBox.setItems(tempObsList);
        stageManager = StageManager.getStageManager();
    }

     public void setStage(Stage stage) {
        stageManager.addStage(WindowsEnum.EDIT_COMPOUND_WINDOW, stage);
        this.stage = stage;
    }

    @FXML
    protected void onCancelButtonClicked(ActionEvent event) {
        stageManager.removeStage(WindowsEnum.EDIT_COMPOUND_WINDOW);
        stage.close();
        event.consume();
    }

    @FXML
    protected void onDeleteCompoundClicked(ActionEvent event) {
        BufferExecutor bufferExecutor = BufferExecutor.getBufferExecutor();
        int compoundIndex = bufferExecutor.getIndexOfCompound(compound);
        Map<Integer, Compound> mapOfCompound = new TreeMap<>();
        mapOfCompound.put(compoundIndex, compound);
        bufferExecutor.addChange(ActionType.REMOVE, mapOfCompound, null, null);
        stageManager.removeStage(WindowsEnum.EDIT_COMPOUND_WINDOW);
        stage.close();
        event.consume();
    }

    @FXML
    protected void onSaveButtonClicked(ActionEvent event) {
        saveChanges();
        stageManager.removeStage(WindowsEnum.EDIT_COMPOUND_WINDOW);
        stage.close();
        event.consume();
    }

    private void saveChanges() {
        float amount;
        String amountString = amountShowEdit.getText();
        if (!matchesFloatPattern(amountString)) {
            ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_NUMBER_FORMAT);
            amountShowEdit.requestFocus();
            return;
        }
        else amount = Float.parseFloat(amountString);
        String newSmiles = smilesShowEdit.getText();
        if ( newSmiles == null || newSmiles.equals("")) {
            ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_SMILES);
            smilesShowEdit.requestFocus();
            return;
        }
        BufferExecutor bufferExecutor = BufferExecutor.getBufferExecutor();
        int compoundIndex = bufferExecutor.getIndexOfCompound(compound);
        Map<Integer, Compound> mapOfCompound = new TreeMap<>();
        mapOfCompound.put(compoundIndex, compound);
        Set<Change> changeSet = new HashSet<>();

        if ( !Float.valueOf(amount).equals(compound.getAmount()) )
            changeSet.add(new Change( ActionType.EDIT, mapOfCompound, Field.AMOUNT, amount));

        if ( !compound.getSmiles().equals(newSmiles) )
            changeSet.add(new Change(ActionType.EDIT, mapOfCompound, Field.SMILES, newSmiles));

        String newCompoundNumber = compoundNumberShowEdit.getText();
        if ( !compound.getCompoundNumber().equals(newCompoundNumber) )
            changeSet.add(new Change(ActionType.EDIT, mapOfCompound, Field.COMPOUNDNUMBER, newCompoundNumber));

        String newUnitString = unitChoiceBox.getValue();
        Unit newUnit = Unit.stringToEnum( newUnitString );
        if ( !compound.getUnit().equals(newUnit) )
            changeSet.add(new Change( ActionType.EDIT, mapOfCompound, Field.UNIT, newUnitString));

        String newForm = formShowEdit.getText();
        if ( !compound.getForm().equals(newForm) )
            changeSet.add(new Change(ActionType.EDIT, mapOfCompound, Field.FORM, newForm));

        String newTempString = tempStabilityChoiceBox.getValue();
        TempStability newTemp = TempStability.stringToEnum( newTempString );
        if ( !compound.getTempStability().equals(newTemp) )
            changeSet.add(new Change( ActionType.EDIT, mapOfCompound, Field.TEMPSTABILITY, newTempString));

        boolean newArgon = argonCheckBox.isSelected();
        if (compound.isArgon() != newArgon)
            changeSet.add(new Change(ActionType.EDIT, mapOfCompound, Field.ARGON, newArgon));

        String newContainer = containerShowEdit.getText();
        if ( !compound.getContainer().equals(newContainer) )
            changeSet.add(new Change( ActionType.EDIT, mapOfCompound, Field.CONTAINER, newContainer));

        String newStorage = storagePlaceShowEdit.getText();
        if ( !compound.getStoragePlace().equals(newStorage) )
            changeSet.add(new Change( ActionType.EDIT, mapOfCompound, Field.STORAGEPLACE, newStorage));

        String newAdditionalInfo = additionalInfoShowEdit.getText();
        if ( !compound.getAdditionalInfo().equals(newAdditionalInfo) )
            changeSet.add(new Change( ActionType.EDIT, mapOfCompound, Field.ADDITIONALINFO, newAdditionalInfo));

        bufferExecutor.addSetOfChanges(changeSet);
    }

    private boolean matchesFloatPattern(String string) {
        boolean resultInt = Pattern.matches("[0-9]+", string);
        boolean resultFloat = Pattern.matches("[0-9]*[.][0-9]+", string);
        return resultInt || resultFloat;
    }

    public void setSelectedItem(Compound selectedCompound) {
        compound = selectedCompound;
        smilesShowEdit.setText(selectedCompound.getSmiles());
        compoundNumberShowEdit.setText(selectedCompound.getCompoundNumber());
        amountShowEdit.setText( String.valueOf( selectedCompound.getAmount() ) );
        formShowEdit.setText(selectedCompound.getForm());
        containerShowEdit.setText(selectedCompound.getContainer());
        storagePlaceShowEdit.setText(selectedCompound.getStoragePlace());
        additionalInfoShowEdit.setText(selectedCompound.getAdditionalInfo());
        argonCheckBox.setSelected(selectedCompound.isArgon());
        unitChoiceBox.setValue(selectedCompound.getUnit().toString());
        tempStabilityChoiceBox.setValue(selectedCompound.getTempStability().toString());
    }
}















