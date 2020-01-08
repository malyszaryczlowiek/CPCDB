package com.github.malyszaryczlowiek.cpcdb.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SearchCompoundStageController implements Initializable
{
    private Stage thisStage;
    private MainStageController controller;

    //@FXML private Button cancelButton;
    //@FXML private Button searchButton;

    @FXML private TextField searchSmiles;
    @FXML private TextField searchCompoundNumber;
    @FXML private TextField searchForm;
    @FXML private TextField searchContainer;
    @FXML private TextField searchStoragePlace;
    @FXML private TextArea additionalInfoTextArea;

    @FXML private DatePicker searchDatePicker;

    @FXML private ComboBox<String> searchSmilesComboBox;
    @FXML private ComboBox<String> compoundNumberComboBox;
    @FXML private ComboBox<String> searchBeforeAfter;
    @FXML private ComboBox<String> searchArgonStability;
    @FXML private ComboBox<String> searchTempStability;

    private LocalDate selectedLocalDate;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        String[] listOfAccuracy = {"Is Exactly", "Is Containing"};
        List<String> list = Arrays.stream(listOfAccuracy).collect(Collectors.toList());
        ObservableList<String> observableList = FXCollections.observableList(list);
        searchSmilesComboBox.setItems(observableList);
        searchSmilesComboBox.setValue(listOfAccuracy[0]); // Is Exactly
        compoundNumberComboBox.setItems(observableList);
        compoundNumberComboBox.setValue(listOfAccuracy[1]);


        String[] beforeAfterArray = {"Before", "After"};
        List<String> beforeAfterList = Arrays.stream(beforeAfterArray).collect(Collectors.toList());
        ObservableList<String> beforeAfterObservableList = FXCollections.observableList(beforeAfterList);
        searchBeforeAfter.setItems(beforeAfterObservableList);
        searchBeforeAfter.setValue(beforeAfterArray[0]); // Before

        String[] argonArray = {"Any Atmosphere", "Without Argon", "Under Argon"};
        List<String> argonList = Arrays.stream(argonArray).collect(Collectors.toList());
        ObservableList<String> argonObservableList = FXCollections.observableList(argonList);
        searchArgonStability.setItems(argonObservableList);
        searchArgonStability.setValue(argonArray[0]);

        String[] tempStabilityArray = {"Any Temperature", "RT", "Fridge", "Freezer"};
        List<String> tempStabilityList = Arrays.stream(tempStabilityArray).collect(Collectors.toList());
        ObservableList<String> tempStabilityObservableList = FXCollections.observableList(tempStabilityList);
        searchTempStability.setItems(tempStabilityObservableList);
        searchTempStability.setValue(tempStabilityArray[0]);

        LocalDate today = LocalDate.now();
        searchDatePicker.setValue(today);
        selectedLocalDate = LocalDate.now();
    }

    public void setStage(Stage stage)
    {
        thisStage = stage;
    }

    @FXML
    protected void onSearchButtonClicked(ActionEvent actionEvent)
    {
        String smiles = searchSmiles.getText();
        String smilesAccuracy = searchSmilesComboBox.getValue();
        String compoundNumber = searchCompoundNumber.getText();
        String compoundNumberAccuracy = compoundNumberComboBox.getValue();
        String form = searchForm.getText();
        String container = searchContainer.getText();
        String storagePlace = searchStoragePlace.getText();
        String beforeAfter = searchBeforeAfter.getValue();
        selectedLocalDate = searchDatePicker.getValue();
        String argon = searchArgonStability.getValue();
        String temperature = searchTempStability.getValue();
        String additionalInfo = additionalInfoTextArea.getText();

        SearchingCriteriaChosenListener listener = controller; // deleted casting (ChosenSearchingCriteriaListener)
        thisStage.close();
        listener.searchingCriteriaChosen(smiles, smilesAccuracy,compoundNumber, compoundNumberAccuracy, form, container,
                storagePlace, beforeAfter, selectedLocalDate, argon, temperature, additionalInfo);

        actionEvent.consume();
    }

    @FXML
    protected void onCancelButtonClicked(ActionEvent actionEvent)
    {
        thisStage.close();
        actionEvent.consume();
    }

    public void setMainStageControllerObject(MainStageController mainStageControllerObject)
    {
        controller = mainStageControllerObject;
    }


    /*
     * ###############################################
     * METHODS CALLED VIA NODES
     * ###############################################
     */

    @FXML
    protected void onDatePickerDateEdited()
    {
        LocalDate selectedDay = searchDatePicker.getValue();

        if (selectedDay.isAfter(LocalDate.now()))
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setResizable(true);

            //alert.setWidth(700);
            //alert.setHeight(550);
            alert.setTitle("Error");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setHeaderText("You cannot set Future date!");
            alert.setContentText("Please set today's or past date.");
            alert.showAndWait();

            searchDatePicker.setValue(selectedLocalDate);
            searchDatePicker.requestFocus();
        }
        else
        { // jeśli data jest dobra to przypisz tę datę
            selectedLocalDate = searchDatePicker.getValue();
            searchArgonStability.requestFocus();
        }
    }


    /*
     * ###############################################
     * INTERFACES
     * ###############################################
     */


    public interface SearchingCriteriaChosenListener
    {
        void searchingCriteriaChosen(String smiles, String smilesAccuracy,
                                     String compoundNumber, String compoundNumberAccuracy,
                                     String form, String container, String storagePlace,
                                     String beforeAfter, LocalDate selectedLocalDate,
                                     String argon, String temperature, String additionalInfo);
    }
}





















