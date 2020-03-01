package com.github.malyszaryczlowiek.cpcdb.initializers;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;
import com.github.malyszaryczlowiek.cpcdb.compound.TempStability;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.cell.ComboBoxTableCell;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TemperatureStabilityColumnInitializer extends ColumnInitializer implements Initializable
{
    private TableColumn<Compound, String> tempStabilityCol;

    public TemperatureStabilityColumnInitializer(TableColumn<Compound, String> tempStabilityCol) {
        this.tempStabilityCol = tempStabilityCol;
    }

    @Override
    public void initialize() {
        tempStabilityCol.setCellValueFactory(
                (TableColumn.CellDataFeatures<Compound, String> compoundStringCellDataFeatures) -> {
                    Compound compound = compoundStringCellDataFeatures.getValue();
                    String stability = compound.getTempStability().toString();
                    return new SimpleStringProperty(stability);
                } );
        List<String> tempStabilityList = Arrays.stream(TempStability.values())
                .map(TempStability::toString)
                .collect(Collectors.toList());
        ObservableList<String> observableTempStabilityList = FXCollections.observableArrayList( tempStabilityList );
        tempStabilityCol.setCellFactory( ComboBoxTableCell.forTableColumn(observableTempStabilityList) );
        tempStabilityCol.setOnEditCommit(
                ( TableColumn.CellEditEvent<Compound,String> event ) -> {
                    TablePosition<Compound,String> position = event.getTablePosition();
                    String newStability = event.getNewValue();
                    int row = position.getRow();
                    Compound compound = event.getTableView().getItems().get(row);
                    if ( !TempStability.stringToEnum(newStability).equals(compound.getTempStability()) )
                        saveChangeToBufferExecutor(compound, Field.TEMPSTABILITY, newStability);
                });
        tempStabilityCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.TemperatureStability") ));
        boolean tempStab =  "true".equals(SecureProperties.getProperty("column.show.TemperatureStability"));
        tempStabilityCol.setVisible( tempStab);
    }
}
