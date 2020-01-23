package com.github.malyszaryczlowiek.cpcdb.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MergingRemoteDbController implements Initializable
{
    private Stage stage;
    private MainStageController mainStageController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }

    @FXML
    protected void onCancelButtonClicked(ActionEvent event) {
        stage.close();
        event.consume();
    }

    @FXML
    protected void onLoadWithoutMergingClicked(ActionEvent event) {
        Mergeable listener = mainStageController;
        listener.loadFromRemoteWithoutMerging();
        stage.close();
        event.consume();
    }

    // TODO tutaj trzeba zaimplementować aby uruchomił sie service który merguje
    @FXML
    protected void onMergeButtonClicked(ActionEvent event) {
        Mergeable listener = mainStageController;
        listener.mergeWithRemote();
        stage.close();
        event.consume();
    }

    public interface Mergeable {
        void mergeWithRemote();
        void loadFromRemoteWithoutMerging();
    }
}
