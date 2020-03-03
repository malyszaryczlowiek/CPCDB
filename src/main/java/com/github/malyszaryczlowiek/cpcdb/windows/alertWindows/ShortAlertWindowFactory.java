package com.github.malyszaryczlowiek.cpcdb.windows.alertWindows;

import javafx.scene.control.Alert;

public class ShortAlertWindowFactory
{
    public static void showErrorWindow(ErrorType errorType) {
        switch (errorType) {
            case CANNOT_CONNECT_TO_ALL_DB:
                new FatalDbConnectionError(Alert.AlertType.ERROR).show();
                break;
            case CANNOT_CONNECT_TO_REMOTE_BD_AND_INCORRECT_LOCAL_PASSPHRASE:
                new CannotConnectToRemoteAndIncorrectLocalUsernameOrPassphrase(Alert.AlertType.ERROR).show();
                break;
            case CANNOT_CONNECT_TO_REMOTE_DB:
                new RemoteServerConnectionError(Alert.AlertType.WARNING).show();
                break;
            case INCORRECT_REMOTE_PASSPHRASE_AND_CANNOT_CONNECT_TO_LOCAL_DB:
                new RemoteServerPassphraseErrorAndCannotConnectToLocalDatabase(Alert.AlertType.ERROR).show();
                break;
            case INCORRECT_REMOTE_AND_LOCAL_PASSPHRASE:
                new IncorrectRemoteAndLocalUsernameOrPassphrase(Alert.AlertType.ERROR).show();
                break;
            case INCORRECT_REMOTE_PASSPHRASE:
                new IncorrectRemotePassphrase(Alert.AlertType.WARNING).show();
                break;
            case UNKNOWN_ERROR_OCCURRED:
                new UnknownErrorOccurred(Alert.AlertType.ERROR).show();
                break;
            case INCORRECT_PORT_NUMBER_FORMAT:
                new IncorrectPortNumberFormat(Alert.AlertType.ERROR).show();
                break;
            case NOT_FOUND_COMPOUND:
                new NotFoundCompound(Alert.AlertType.INFORMATION).show();
                break;
            case CANNOT_CONNECT_TO_LOCAL_DB:
                new UpdatingErrorCannotConnectToDb(Alert.AlertType.ERROR).show();
                break;
            case INCORRECT_LOCAL_DB_AUTHORISATION:
                new IncorrectLocalDbAuthorisation(Alert.AlertType.ERROR).show();
                break;
            case INCORRECT_NUMBER_FORMAT:
                new IncorrectNumberFormat(Alert.AlertType.ERROR).show();
                break;
            case INCORRECT_SMILES:
                new IncorrectSmiles(Alert.AlertType.ERROR).show();
                break;
            default:
                break;
        }
    }
}
