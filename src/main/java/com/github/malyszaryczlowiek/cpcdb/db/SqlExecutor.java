package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class SqlExecutor
{
    private List<Field> listOfFieldsToChange;
    private Compound compound;
    private Connection connection;

    public SqlExecutor(Compound compound, Connection connection) {
        this.compound = compound;
        this.connection = connection;
    }

    public void executeUpdate() {
        String stringStatement = prepareStatement(compound);
        try {
            PreparedStatement statement = connection.prepareStatement(stringStatement);
            int index = 1;
            for (Field field: listOfFieldsToChange) {
            /*
                Lepiej jest pozostawić wprowadzanie danych na poziomie budowania
                PreparedStatement bo to ami lepiej sobie poradzi z wprowadzaniem
                takich danych jak np LocalDateTime niż jakbyśmy tempo przeklejali
                je tutaj w postaci stringów.
                 */
                switch (field) {
                    case SMILES:
                        statement.setString(index, compound.getSmiles());
                        break;
                    case COMPOUNDNUMBER:
                        statement.setString(index, compound.getCompoundNumber());
                        break;
                    case AMOUNT:
                        statement.setFloat(index, compound.getAmount());
                        break;
                    case UNIT:
                        statement.setString(index, compound.getUnit().toString());
                        break;
                    case FORM:
                        statement.setString(index, compound.getForm());
                        break;
                    case TEMPSTABILITY:
                        statement.setString(index, compound.getTempStability().toString());
                        break;
                    case ARGON:
                        statement.setBoolean(index, compound.isArgon());
                        break;
                    case CONTAINER:
                        statement.setString(index, compound.getContainer());
                        break;
                    case STORAGEPLACE:
                        statement.setString(index, compound.getStoragePlace());
                        break;
                    case DATETIMEMODIFICATION:
                        statement.setTimestamp(index, Timestamp.valueOf(compound.getDateTimeModification()));//
                        break;
                    case ADDITIONALINFO:
                        statement.setString(index, compound.getAdditionalInfo());
                        break;
                    default:
                        break;
                }
                ++index;
            }
            statement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeDelete() {
        String query = "DELETE FROM compounds WHERE CompoundID = " + compound.getId();
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeInsert() {
        // tutaj robimy sprawdzanie wszelkich danych wejściowych
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

        String insertQuery = "INSERT INTO compounds(Smiles, CompoundNumber, Amount, Unit, " +
                "Form, Stability, Argon, Container, " +
                "StoragePlace, LastModification, AdditionalInfo) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement addingStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);

            addingStatement.setString(1, smiles);
            addingStatement.setString(2, compoundNumber);
            addingStatement.setFloat(3, amount);

            addingStatement.setString(4, unit);
            addingStatement.setString(5, form);
            addingStatement.setString(6, stability);

            addingStatement.setBoolean(7, argon);
            addingStatement.setString(8, container);
            addingStatement.setString(9, storagePlace);

            addingStatement.setTimestamp(10, Timestamp.valueOf(modificationDate));
            addingStatement.setString(11, additionalInformation);

            //int rawAffected = addingStatement.executeUpdate();
            addingStatement.executeUpdate();

            /*
            if (rawAffected == 1)
            {
                System.out.println("added one item");

            }
            else
                System.out.println("added different than one number of items");
             */
            String loadLastAddedItemId = "SELECT LAST_INSERT_ID()";
            PreparedStatement loadDBStatement = connection.prepareStatement(loadLastAddedItemId);
            ResultSet resultSet = loadDBStatement.executeQuery();
            // to mi zwraca raw gdzie w kolumnie CompoundId mam największą wartoś
            // dlatego muszę tę wartość już tylko wyłuskać. Robię to używająć metody
            // getInt(1) bo pobieram wartość z pierwszej kolumny.

            resultSet.next();
            int generatedId = resultSet.getInt(1);
            compound.setId(generatedId);
            compound.setSavedInDatabase(true);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private String prepareStatement(Compound compound) {
        listOfFieldsToChange = compound.getListOfOrderedFieldsToChange();
        StringBuilder updateQueryBuilder = new StringBuilder("UPDATE compounds  SET ");
        for (Field field: listOfFieldsToChange) {
            /*
            Lepiej jest pozostawić wprowadzanie danych na poziomie budowania
            PreparedStatement bo to ami lepiej sobie poradzi z wprowadzaniem
            takich danych jak np LocalDateTime niż jakbyśmy tempo przeklejali
            je tutaj w postaci stringów.
             */
            switch (field) {
                case SMILES:
                    updateQueryBuilder.append("Smiles = ?, ");
                    break;
                case COMPOUNDNUMBER:
                    updateQueryBuilder.append("CompoundNumber = ?, ");
                    break;
                case AMOUNT:
                    updateQueryBuilder.append("Amount = ?, ");
                    break;
                case UNIT:
                    updateQueryBuilder.append("Unit = ?, ");
                    break;
                case FORM:
                    updateQueryBuilder.append("Form = ?, ");
                    break;
                case TEMPSTABILITY:
                    updateQueryBuilder.append("Stability = ?, ");
                    break;
                case ARGON:
                    updateQueryBuilder.append("Argon = ?, ");
                    break;
                case CONTAINER:
                    updateQueryBuilder.append("Container = ?, ");
                    break;
                case STORAGEPLACE:
                    updateQueryBuilder.append("StoragePlace = ?, ");
                    break;
                case DATETIMEMODIFICATION:
                    updateQueryBuilder.append("LastModification = ?, ");
                    break;
                case ADDITIONALINFO:
                    updateQueryBuilder.append("AdditionalInfo = ?, ");
                    break;
                default:
                    break;
            }
        }
        int characterNumber = updateQueryBuilder.length();
        updateQueryBuilder.delete(characterNumber -2, characterNumber -1);

        updateQueryBuilder.append(" WHERE CompoundID = ");
        updateQueryBuilder.append(compound.getId());

        return updateQueryBuilder.toString();
    }
}











