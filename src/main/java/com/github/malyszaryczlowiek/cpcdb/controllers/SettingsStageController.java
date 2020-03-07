package com.github.malyszaryczlowiek.cpcdb.controllers;

import com.github.malyszaryczlowiek.cpcdb.helperClasses.LaunchTimer;
import com.github.malyszaryczlowiek.cpcdb.managers.ColumnManager;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;


public class SettingsStageController implements Initializable
{
    private Stage stage;
    private final String[] duration = {"Always", "Year", "Quarter", "Month", "Week", "Day", "Session"};
    private final String[] fontSize = {"table-view-size13", "table-view-size14", "table-view-size15",
            "table-view-size16", "table-view-size17", "table-view-size18", "table-view-size19", "table-view-size20",
            "table-view-size21", "table-view-size22", "table-view-size23", "table-view-size24"};
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

    @FXML private Slider keyValidityDurationSlider;
    @FXML private Label keyValidityDurationLabel;

    @FXML private Slider fontSizeSlider;
    @FXML private Label fontSizeLabel;
    @FXML private CheckBox connectToLocalServerCheckBox;

    private ColumnManager columnManager;
    private String oldFontSize;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LaunchTimer timer = new LaunchTimer();
        Task<Void> task1 = new Task<>() {
            @Override protected Void call()            {
                remoteServerAddressIP.setText( SecureProperties.getProperty("settings.db.remote.serverAddressIP") );
                remotePortNumber.setText( SecureProperties.getProperty("settings.db.remote.portNumber") );
                remoteUser.setText( SecureProperties.getProperty("settings.db.remote.user") );
                remotePassphrase.setText( SecureProperties.getProperty("settings.db.remote.passphrase") );
                remoteServerConnectorSettingsCheckBox.setSelected( SecureProperties.getProperty(
                        "settings.db.remote.connectorConfiguration.useRemoteConnectorServerSettings").equals("true") );
                remoteUseUnicode.setSelected( SecureProperties.getProperty(
                        "settings.db.remote.connectorConfiguration.useUnicode").equals("true") );
                remoteUseJDBCCompilantTimeZone.setSelected( SecureProperties.getProperty(
                        "settings.db.remote.connectorConfiguration.useJDBCCompilantTimezoneShift").equals("true") );
                remoteUseLegacyDatetimeMode.setSelected( SecureProperties.getProperty(
                                "settings.db.remote.connectorConfiguration.useLegacyDateTimeCode").equals("true") );
                remoteServerTimeZone.setItems( FXCollections.observableList( Arrays.asList(timeZones) ) );
                remoteServerTimeZone.setValue( SecureProperties.getProperty(
                                "settings.db.remote.connectorConfiguration.serverTimezone") );
                useRemoteServerConnectorCheckBoxClicked();
                return null;
            }
        };

        Task<Void> task2 = new Task<>()
        {
            @Override
            protected Void call() {
                columnManager = new ColumnManager();
                localPortNumber.setText( SecureProperties.getProperty("settings.db.local.portNumber") );
                localUser.setText( SecureProperties.getProperty("settings.db.local.user") );
                localPassphrase.setText( SecureProperties.getProperty("settings.db.local.passphrase") );
                localServerConnectorSettingsCheckBox.setSelected( SecureProperties.getProperty(
                        "settings.db.local.connectorConfiguration.useLocalConnectorServerSettings").equals("true") );
                localUseUnicode.setSelected( SecureProperties.getProperty(
                                "settings.db.local.connectorConfiguration.useUnicode").equals("true") );
                localUseJDBCCompilantTimeZone.setSelected( SecureProperties.getProperty(
                        "settings.db.local.connectorConfiguration.useJDBCCompilantTimezoneShift").equals("true") );
                localUseLegacyDatetimeMode.setSelected( SecureProperties.getProperty(
                        "settings.db.local.connectorConfiguration.useLegacyDateTimeCode").equals("true") );
                localServerTimeZone.setItems( FXCollections.observableList( Arrays.asList(timeZones) ) );
                localServerTimeZone.setValue( SecureProperties.getProperty(
                        "settings.db.local.connectorConfiguration.serverTimezone") );
                boolean connectToLocalDb = SecureProperties.getProperty("tryToConnectToLocalDb").equals("true");
                connectToLocalServerCheckBox.setSelected( connectToLocalDb);
                if (connectToLocalDb) connectToLocalServerCheckBox.setText("True");
                else connectToLocalServerCheckBox.setText("False");
                useLocalServerConnectorCheckBoxClicked();
                switch ( SecureProperties.getProperty("settings.keyValidityDuration") ) {
                    case "always":
                        keyValidityDurationSlider.adjustValue(.0);
                        keyValidityDurationLabel.setText(duration[0]);
                        break;
                    case "year":
                        keyValidityDurationSlider.adjustValue(1.0);
                        keyValidityDurationLabel.setText(duration[1]);
                        break;
                    case "quarter":
                        keyValidityDurationSlider.adjustValue(2.0);
                        keyValidityDurationLabel.setText(duration[2]);
                        break;
                    case "month":
                        keyValidityDurationSlider.adjustValue(3.0);
                        keyValidityDurationLabel.setText(duration[3]);
                        break;
                    case "week":
                        keyValidityDurationSlider.adjustValue(4.0);
                        keyValidityDurationLabel.setText(duration[4]);
                        break;
                    case "day":
                        keyValidityDurationSlider.adjustValue(5.0);
                        keyValidityDurationLabel.setText(duration[5]);
                        break;
                    case "session":
                        keyValidityDurationSlider.adjustValue(6.0);
                        keyValidityDurationLabel.setText(duration[6]);
                        break;
                    default:
                        keyValidityDurationLabel.setText(duration[0]);
                        break;
                }
                settingUpFontSizeConfiguration();
                keyValidityDurationSlider.valueProperty()
                        .addListener( (ObservableValue<? extends Number> observableValue, Number number, Number t1) -> {
                            int index = t1.intValue();
                            keyValidityDurationLabel.setText(duration[index]);
                            long l = Math.round( t1.doubleValue() );
                            keyValidityDurationSlider.setValue((double) l);
                        }
                );
                fontSizeSlider.valueProperty().addListener( (observable, oldValue, newValue) -> {
                    int newIndex = newValue.intValue();
                    fontSizeLabel.setText( newIndex + " px");
                    long l = Math.round( newValue.doubleValue() );
                    fontSizeSlider.setValue((double) l);
                    columnManager.changeFontSize(fontSize, fontSize[newIndex-13]);
                });
                connectToLocalServerCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) connectToLocalServerCheckBox.setText("True");
                    else connectToLocalServerCheckBox.setText("False");
                });
                return null;
            }
        };
        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);
        thread1.setDaemon(true);
        thread2.setDaemon(true);
        thread1.start();
        thread2.start();
        timer.stopTimer("czas inicjalizacji otwierania okna ustawień z 2ma wątkami");
    }

    @FXML
    private void onSaveButtonClicked() {
        Task<Void> task0 = new Task<>()
        {
            @Override
            protected Void call() {
                settingUpRemoteServerConfiguration();
                int selectedFontSize = (int) fontSizeSlider.getValue();
                String size = fontSize[selectedFontSize - 13];
                SecureProperties.setProperty("settings.fontSize", size);
                return null;
            }
        };
        Task<Integer> task1 = new Task<>()
        {
            @Override
            protected Integer call() {
                settingUpLocalServerConfiguration();
                int selectedValidityDuration = (int) keyValidityDurationSlider.getValue();
                String dur = duration[selectedValidityDuration].toLowerCase();
                SecureProperties.setProperty("settings.keyValidityDuration", dur);
                return null;
            }
        };
        Thread thread0 = new Thread(task0);
        Thread thread1 = new Thread(task1);
        thread0.setDaemon(true);
        thread1.setDaemon(true);
        thread0.start();
        thread1.start();
        stage.close();
    }

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

    @FXML
    private void onCancelButtonClicked() {
        columnManager.changeFontSize(fontSize, oldFontSize);
        stage.close();
    }

    public void setStage(Stage stage) { this.stage = stage; }

    private void settingUpRemoteServerConfiguration() {
        String remoteServerAddressIPString = remoteServerAddressIP.getText().trim();
        String remotePortNumberString = remotePortNumber.getText().trim();
        String remoteUserNameString = remoteUser.getText().trim();
        String remotePassphraseString = remotePassphrase.getText().trim();
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
                remoteConnectorConfigurationServerTimezone
        );
    }

    private void settingUpLocalServerConfiguration() {
        String localPortNumberString = localPortNumber.getText().trim();
        String localUserNameString = localUser.getText().trim();
        String localPassphraseString = localPassphrase.getText().trim();
        SecureProperties.setProperty("settings.db.local.portNumber", localPortNumberString);
        SecureProperties.setProperty("settings.db.local.user", localUserNameString);
        SecureProperties.setProperty("settings.db.local.passphrase", localPassphraseString);
        boolean localConnectorConfigurationUseUnicode = localUseUnicode.isSelected();
        boolean localConnectorConfigurationUseJDBCCompilantTimezoneShift = localUseJDBCCompilantTimeZone.isSelected();
        boolean localConnectorConfigurationUseLegacyDateTimeCode = localUseLegacyDatetimeMode.isSelected();
        String localConnectorConfigurationServerTimezone = localServerTimeZone.getValue();
        boolean connectToLocalDb = connectToLocalServerCheckBox.isSelected();
        SecureProperties.setProperty( "settings.db.local.connectorConfiguration.useLocalConnectorServerSettings",
                Boolean.toString( localServerConnectorSettingsCheckBox.isSelected() ) );
        SecureProperties.setProperty( "settings.db.local.connectorConfiguration.useUnicode",
                Boolean.toString( localConnectorConfigurationUseUnicode ) );
        SecureProperties.setProperty("settings.db.local.connectorConfiguration.useJDBCCompilantTimezoneShift",
                Boolean.toString( localConnectorConfigurationUseJDBCCompilantTimezoneShift ) );
        SecureProperties.setProperty( "settings.db.local.connectorConfiguration.useLegacyDateTimeCode",
                Boolean.toString( localConnectorConfigurationUseLegacyDateTimeCode ) );
        SecureProperties.setProperty( "settings.db.local.connectorConfiguration.serverTimezone",
                localConnectorConfigurationServerTimezone );
        SecureProperties.setProperty("tryToConnectToLocalDb", Boolean.toString(connectToLocalDb));
    }

    private void settingUpFontSizeConfiguration() throws IllegalArgumentException{
        oldFontSize = SecureProperties.getProperty("settings.fontSize");
        switch (oldFontSize) {
            case "table-view-size13":
                fontSizeSlider.adjustValue(13.0);
                fontSizeLabel.setText("13 px");
                break;
            case "table-view-size14":
                fontSizeSlider.adjustValue(14.0);
                fontSizeLabel.setText("14 px");
                break;
            case "table-view-size15":
                fontSizeSlider.adjustValue(15.0);
                fontSizeLabel.setText("15 px");
                break;
            case "table-view-size16":
                fontSizeSlider.adjustValue(16.0);
                fontSizeLabel.setText("16 px");
                break;
            case "table-view-size17":
                fontSizeSlider.adjustValue(17.0);
                fontSizeLabel.setText("17 px");
                break;
            case "table-view-size18":
                fontSizeSlider.adjustValue(18.0);
                fontSizeLabel.setText("18 px");
                break;
            case "table-view-size19":
                fontSizeSlider.adjustValue(19.0);
                fontSizeLabel.setText("19 px");
                break;
            case "table-view-size20":
                fontSizeSlider.adjustValue(20.0);
                fontSizeLabel.setText("20 px");
                break;
            case "table-view-size21":
                fontSizeSlider.adjustValue(21.0);
                fontSizeLabel.setText("21 px");
                break;
            case "table-view-size22":
                fontSizeSlider.adjustValue(22.0);
                fontSizeLabel.setText("22 px");
                break;
            case "table-view-size23":
                fontSizeSlider.adjustValue(23.0);
                fontSizeLabel.setText("23 px");
                break;
            case "table-view-size24":
                fontSizeSlider.adjustValue(24.0);
                fontSizeLabel.setText("24 px");
                break;
            default:
                throw new IllegalArgumentException("coś jest nie tak z przekazaną daną do slidera");
        }
    }
}






// to było w set Stage
/*
        Scene scene = stage.getScene();
        scene.widthProperty().addListener(
                (observableValue, number, t1) -> {
                    double width = (double) t1;
                    double innerAnchorPaneMinWidth = innerAnchorPane.getMinWidth();
                    innerAnchorPane.prefWidthProperty().setValue(width);
                    if (width > innerAnchorPaneMinWidth)
                        innerAnchorPane.prefWidthProperty().setValue(width);
                });
         */

/*
    Arrays.stream(Field.values())
                                    .forEach(
                                            field -> {
                                                ObservableList<String> styleClass = columnManager.getColumn(field).getStyleClass();
                                                styleClass.removeAll(fontSize);
                                                styleClass.add(fontSize[newIndex-13]);
                                            });
     */

