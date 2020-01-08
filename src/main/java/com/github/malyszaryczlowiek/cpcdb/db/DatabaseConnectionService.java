package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;

import com.github.malyszaryczlowiek.cpcdb.compound.TempStability;
import com.github.malyszaryczlowiek.cpcdb.compound.Unit;

import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * clasa która implementuje to jak sie zmienia dostęp do połączenia z internetem
 */
public class DatabaseConnectionService extends Service<ObservableList<Compound>>
{
    @Override
    protected Task<ObservableList<Compound>> createTask()
    {
        return new Task<>()
        {
            @Override
            protected ObservableList<Compound> call()
            {
                String loadDBSQLQuery = "SELECT * FROM compounds";

                try (Connection connection = ConnectionManager.connectToDb())
                {
                    PreparedStatement loadDBStatement = connection.prepareStatement(loadDBSQLQuery);
                    ResultSet resultSet = loadDBStatement.executeQuery();

                    ArrayList<Compound> fullListOfCompounds = new ArrayList<>();

                    while(resultSet.next())
                    {
                        int id = resultSet.getInt(1);
                        String smiles = resultSet.getString(2);
                        String compoundName = resultSet.getString(3);
                        float amount = resultSet.getFloat(4);
                        String unit = resultSet.getString(5);
                        String form = resultSet.getString(6);
                        String tempStability = resultSet.getString(7);
                        boolean argon = resultSet.getBoolean(8);
                        String container = resultSet.getString(9);
                        String storagePlace = resultSet.getString(10);
                        LocalDateTime dateTimeModification = resultSet.getTimestamp(11).toLocalDateTime();
                        String additionalInformation = resultSet.getString(12);

                        Compound compound = new Compound(smiles, compoundName, amount, Unit.stringToEnum(unit),
                                form, TempStability.stringToEnum(tempStability), argon, container,
                                storagePlace, dateTimeModification, additionalInformation);
                        compound.setId(id);
                        compound.setSavedInDatabase(true);

                        fullListOfCompounds.add(compound);
                    }
                    // TODO to trzeba pociągnąć dalej
                    // observableList = FXCollections.observableArrayList(fullListOfCompounds);

                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }

                return null;
            }

        };
    }
}
