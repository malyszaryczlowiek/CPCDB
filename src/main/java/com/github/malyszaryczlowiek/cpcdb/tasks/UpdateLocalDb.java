package com.github.malyszaryczlowiek.cpcdb.tasks;

import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlags;
import com.github.malyszaryczlowiek.cpcdb.alerts.ErrorFlagsManager;
import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.db.ConnectionManager;
import com.github.malyszaryczlowiek.cpcdb.managers.CurrentStatusManager;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ErrorType;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.ShortAlertWindowFactory;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.sql.*;
import java.time.LocalDateTime;

public class UpdateLocalDb extends Task<Void>
{
    private ObservableList<Compound> observableList;

    public static Task<Void> getTask(ObservableList<Compound> observableList) {
        UpdateLocalDb updateLocalDb = new UpdateLocalDb(observableList);
        updateLocalDb.setUpTaskListeners();
        return updateLocalDb;
    }

    private void setUpTaskListeners() {
        this.messageProperty().addListener((observable2, oldValue2, newValue2) -> {
            CurrentStatusManager currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager();
            switch (newValue2) {
                case "connectionError":
                    ShortAlertWindowFactory.showErrorWindow(ErrorType.CANNOT_CONNECT_TO_LOCAL_DB);
                    currentStatusManager.setErrorMessage("Error, Cannot Connect to Local Database");
                    break;
                case "incorrectPassphrase":
                    ShortAlertWindowFactory.showErrorWindow(ErrorType.INCORRECT_LOCAL_DB_AUTHORISATION);
                    currentStatusManager.setCurrentStatus("Error, Incorrect Local Database Authorisation");
                    break;
                case "updatingLocalDatabase":
                    currentStatusManager.resetFont();
                    currentStatusManager.setCurrentStatus("Updating Local Database");
                    break;
                case "localDbUpdated":
                    currentStatusManager.resetFont();
                    currentStatusManager.setCurrentStatus("Local Database Updated");
                    break;
                case "UnknownError":
                    ShortAlertWindowFactory.showErrorWindow(ErrorType.UNKNOWN_ERROR_OCCURRED);
                    currentStatusManager.setCurrentStatus("Unknown Error");
                    break;
                default:
                    break;
            }
        });
    }

    private UpdateLocalDb(ObservableList<Compound> observableList) {
        this.observableList = observableList;
    }

    @Override
    protected Void call() {
        try ( Connection connection = ConnectionManager.connectToLocalDb() ) {
            if ( ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR)
                    && connection == null )
                updateMessage("connectionError");
            else if ( ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR)
                    && connection == null )
                updateMessage("incorrectPassphrase");
            else if ( !ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR)
                    && !ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR)
                    && connection != null ) {
                updateMessage("updatingLocalDatabase");
                updateLocalDatabase(connection);
            }
            else
                updateMessage("UnknownError.");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateLocalDatabase(Connection connection) {
        final String clearTableQuery = "DELETE FROM compounds";
        try {
            connection.setAutoCommit(false);
            PreparedStatement loadDBStatement = connection.prepareStatement(clearTableQuery);
            loadDBStatement.executeUpdate();

            for (Compound compound: observableList) {
                Integer compoundId = compound.getId();
                String smiles = compound.getSmiles();
                String compoundNumber = compound.getCompoundNumber();
                float amount = compound.getAmount();
                String unit = compound.getUnit().toString();
                String form = compound.getForm();
                String stability = compound.getTempStability().toString();
                String container = compound.getContainer();
                boolean argon = compound.isArgon();
                String storagePlace = compound.getStoragePlace();
                LocalDateTime modificationDate = compound.getDateTimeModification();
                String additionalInformation = compound.getAdditionalInfo();

                String insertQuery = "INSERT INTO compounds(CompoundID, Smiles, CompoundNumber, Amount, Unit, " +
                        "Form, Stability, Argon, Container, " +
                        "StoragePlace, LastModification, AdditionalInfo) " +
                        "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

                PreparedStatement addingStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);

                addingStatement.setInt(1, compoundId);
                addingStatement.setString(2, smiles);
                addingStatement.setString(3, compoundNumber);
                addingStatement.setFloat(4, amount);

                addingStatement.setString(5, unit);
                addingStatement.setString(6, form);
                addingStatement.setString(7, stability);
                addingStatement.setBoolean(8, argon);

                addingStatement.setString(9, container);
                addingStatement.setString(10, storagePlace);
                addingStatement.setTimestamp(11, Timestamp.valueOf(modificationDate));
                addingStatement.setString(12, additionalInformation);

                addingStatement.executeUpdate();

                // można jeszcze spróbować rozwiązania z
                // https://www.mysqltutorial.org/mysql-jdbc-transaction/
                // gdzie ustawia się na początku po stroworzeniu connection setAutoCommit(false)
                // i wtedy grupujemy wszystkie polecenia do wykonania i robimy jeden commit()
                // na końcu.
            }
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateMessage("localDbUpdated");
        System.out.println("Local Db updated");
    }
}
