package com.github.malyszaryczlowiek.cpcdb.controllers;

import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ErrorType;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ShortAlertWindowFactory;
import com.github.malyszaryczlowiek.cpcdb.util.CloseProgramNotifier;
import com.github.malyszaryczlowiek.cpcdb.helperClasses.LaunchTimer;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class SqlPropertiesStageController implements Initializable
{
    private Stage thisStage;
    private final String[] timeZones = {"Europe/Warsaw"};

    @FXML private TextField remoteServerAddressIP;
    @FXML private TextField remotePortNumber;
    @FXML private TextField remoteUser;
    @FXML private PasswordField remotePassphrase;

    @FXML private TextField localPortNumber;
    @FXML private TextField localUser;
    @FXML private PasswordField localPassphrase;

    @FXML private CheckBox remoteServerConnectorSettingsCheckBox;
    @FXML private CheckBox remoteUseUnicode;
    @FXML private CheckBox remoteUseJDBCCompilantTimeZone;
    @FXML private CheckBox remoteUseLegacyDatetimeMode;
    @FXML private ChoiceBox<String> remoteServerTimeZone;

    @FXML private CheckBox localServerConnectorSettingsCheckBox;
    @FXML private CheckBox localUseUnicode;
    @FXML private CheckBox localUseJDBCCompilantTimeZone;
    @FXML private CheckBox localUseLegacyDatetimeMode;
    @FXML private ChoiceBox<String> localServerTimeZone;

    @FXML Button saveButton;
    @FXML Button cancelButton;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setInput(); // to było wcześniej w onSaveButtonClicked() na początku metody
        remoteServerTimeZone.setItems( FXCollections.observableList( Arrays.asList(timeZones) ) );
        remoteServerTimeZone.setValue(timeZones[0]);
        localServerTimeZone.setItems( FXCollections.observableList( Arrays.asList(timeZones) ) );
        localServerTimeZone.setValue(timeZones[0]);
    }

    @FXML
    protected void onSaveButtonClicked() {
        LaunchTimer timer = new LaunchTimer();
        String remotePortNumberString = remotePortNumber.getText();
        String localPortNumberString = localPortNumber.getText();
        try {
            Integer.parseInt( remotePortNumberString );
            if ( !localPortNumberString.equals("") ) Integer.parseInt( localPortNumberString );
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            ShortAlertWindowFactory.showWindow(ErrorType.INCORRECT_PORT_NUMBER_FORMAT);
            return;
        }
        SecureProperties.loadProperties();
        settingUpRemoteServerConfiguration();
        settingUpLocalServerConfiguration();
        setUpStartingPropertiesOfAllColumns();
        CloseProgramNotifier.setToNotCloseProgram();
        timer.stopTimer("Loading properties when Save Button Clicked during initialization ");
        thisStage.close();
    }

    @FXML
    protected void onCancelButtonClicked() { thisStage.close(); }

    @FXML
    protected void useRemoteServerConnectorCheckBoxClicked() {
        if ( remoteServerConnectorSettingsCheckBox.isSelected() ) {
            remoteUseUnicode.setDisable(false);
            remoteUseJDBCCompilantTimeZone.setDisable(false);
            remoteUseLegacyDatetimeMode.setDisable(false);
            remoteServerTimeZone.setDisable(false);
        } else {
            remoteUseUnicode.setDisable(true);
            remoteUseJDBCCompilantTimeZone.setDisable(true);
            remoteUseLegacyDatetimeMode.setDisable(true);
            remoteServerTimeZone.setDisable(true);
        }
    }

    @FXML
    protected void useLocalServerConnectorCheckBoxClicked() {
        if ( localServerConnectorSettingsCheckBox.isSelected() ) {
            localUseUnicode.setDisable(false);
            localUseJDBCCompilantTimeZone.setDisable(false);
            localUseLegacyDatetimeMode.setDisable(false);
            localServerTimeZone.setDisable(false);
        } else {
            localUseUnicode.setDisable(true);
            localUseJDBCCompilantTimeZone.setDisable(true);
            localUseLegacyDatetimeMode.setDisable(true);
            localServerTimeZone.setDisable(true);
        }
    }

    private void settingUpRemoteServerConfiguration() { // Ustawianie zdalnego servera
        String remoteServerAddressIPString = remoteServerAddressIP.getText();
        String remotePortNumberString = remotePortNumber.getText();
        String remoteUserNameString = remoteUser.getText();
        String remotePassphraseString = remotePassphrase.getText();

        SecureProperties.setProperty("settings.db.remote.RDBMS", "mysql");
        SecureProperties.setProperty("remoteDBExists", "false");
        SecureProperties.setProperty("settings.db.remote.serverAddressIP", remoteServerAddressIPString);
        SecureProperties.setProperty("settings.db.remote.portNumber", remotePortNumberString);
        SecureProperties.setProperty("settings.db.remote.user", remoteUserNameString);
        SecureProperties.setProperty("settings.db.remote.passphrase", remotePassphraseString);

        // Ustawianie zdalnego connectora
        boolean remoteConnectorConfigurationUseUnicode = remoteUseUnicode.isSelected();
        boolean remoteConnectorConfigurationUseJDBCCompilantTimezoneShift = remoteUseJDBCCompilantTimeZone.isSelected();
        boolean remoteConnectorConfigurationUseLegacyDateTimeCode = remoteUseLegacyDatetimeMode.isSelected();

        String remoteConnectorConfigurationServerTimezone = remoteServerTimeZone.getValue();
        SecureProperties.setProperty( "settings.db.remote.connectorConfiguration.useRemoteConnectorServerSettings",
                Boolean.toString( remoteServerConnectorSettingsCheckBox.isSelected() ));
        SecureProperties.setProperty( "settings.db.remote.connectorConfiguration.useUnicode",
                Boolean.toString( remoteConnectorConfigurationUseUnicode ) );
        SecureProperties.setProperty( "settings.db.remote.connectorConfiguration.useJDBCCompilantTimezoneShift",
                Boolean.toString( remoteConnectorConfigurationUseJDBCCompilantTimezoneShift ) );
        SecureProperties.setProperty( "settings.db.remote.connectorConfiguration.useLegacyDateTimeCode",
                Boolean.toString( remoteConnectorConfigurationUseLegacyDateTimeCode ) );
        SecureProperties.setProperty( "settings.db.remote.connectorConfiguration.serverTimezone",
                remoteConnectorConfigurationServerTimezone );
    }

    private void settingUpLocalServerConfiguration() {
        String localPortNumberString = localPortNumber.getText().trim();
        String localUserNameString = localUser.getText().trim();
        String localPassphraseString = localPassphrase.getText().trim();

        SecureProperties.setProperty("settings.db.local.RDBMS", "mysql");
        SecureProperties.setProperty("localDBExists", "false");
        SecureProperties.setProperty("settings.db.local.serverAddressIP", "localhost");
        SecureProperties.setProperty("settings.db.local.portNumber", localPortNumberString);
        SecureProperties.setProperty("settings.db.local.user", localUserNameString);
        SecureProperties.setProperty("settings.db.local.passphrase", localPassphraseString);

        boolean localConnectorConfigurationUseUnicode = localUseUnicode.isSelected();
        boolean localConnectorConfigurationUseJDBCCompilantTimezoneShift = localUseJDBCCompilantTimeZone.isSelected();
        boolean localConnectorConfigurationUseLegacyDateTimeCode = localUseLegacyDatetimeMode.isSelected();

        String localConnectorConfigurationServerTimezone = localServerTimeZone.getValue();
        SecureProperties.setProperty(  "settings.db.local.connectorConfiguration.useLocalConnectorServerSettings",
                Boolean.toString( localServerConnectorSettingsCheckBox.isSelected() ) );
        SecureProperties.setProperty( "settings.db.local.connectorConfiguration.useUnicode",
                Boolean.toString( localConnectorConfigurationUseUnicode ) );
        SecureProperties.setProperty( "settings.db.local.connectorConfiguration.useJDBCCompilantTimezoneShift",
                Boolean.toString( localConnectorConfigurationUseJDBCCompilantTimezoneShift ) );
        SecureProperties.setProperty( "settings.db.local.connectorConfiguration.useLegacyDateTimeCode",
                Boolean.toString( localConnectorConfigurationUseLegacyDateTimeCode ) );
        SecureProperties.setProperty( "settings.db.local.connectorConfiguration.serverTimezone",
                localConnectorConfigurationServerTimezone );
    }

    private void setUpStartingPropertiesOfAllColumns() {
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
        SecureProperties.setProperty("settings.fontSize","table-view-size16");
    }

    public void setStage(Stage stage) { thisStage = stage; }

    //  metoda taka, że nie trzeba za każdym razem wpisywać danych
    private void setInput() {
        remoteServerAddressIP.setText("remotemysql.com");
        remotePortNumber.setText("3306");
        remoteUser.setText("Wa1s8JBvyU");
        remotePassphrase.setText("5YlJQGAuml");
        localPortNumber.setText("3306");
        localUser.setText("root");
        localPassphrase.setText("Janowianka1922?");
        // localServerConfiguration.setText("useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Warsaw");
    }
}









