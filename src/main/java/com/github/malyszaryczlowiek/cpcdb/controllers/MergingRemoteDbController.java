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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    protected void onCancelButtonClicked(ActionEvent event) {
        stage.close();
        event.consume();
    }

    @FXML
    protected void onLoadWithoutMergingClicked() {

    }

    @FXML
    protected void onMergeButtonClicked() {
        // TODO tutaj trzeba zaimplementować aby uruchomił sie service który merguje
    }
}
