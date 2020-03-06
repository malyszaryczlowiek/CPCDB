package com.github.malyszaryczlowiek.cpcdb.controllers;

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
    public void initialize(URL location, ResourceBundle resources) {}

    public void setStage(Stage stage) { this.stage = stage; }

    public void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }

    @FXML
    protected void onCancelButtonClicked() { stage.close(); }

    @FXML
    protected void onLoadWithoutMergingClicked() {
        Mergeable listener = mainStageController;
        listener.loadFromRemoteWithoutMerging();
        stage.close();
    }

    @FXML
    protected void onMergeButtonClicked() {
        Mergeable listener = mainStageController;
        listener.mergeWithRemote();
        stage.close();
    }

    public interface Mergeable {
        void mergeWithRemote();
        void loadFromRemoteWithoutMerging();
    }
}
