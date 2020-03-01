package com.github.malyszaryczlowiek.cpcdb.initializers;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;
import com.github.malyszaryczlowiek.cpcdb.compound.Unit;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.cell.ComboBoxTableCell;

public class UnitColumnInitializer extends ColumnInitializer implements Initializable
{
    private TableColumn<Compound, String> unitCol;

    public UnitColumnInitializer(TableColumn<Compound, String> unitCol) {
        this.unitCol = unitCol;
    }


    @Override
    public void initialize() {
        unitCol.setCellValueFactory(
                (TableColumn.CellDataFeatures<Compound, String> compoundStringCellDataFeatures) -> {
                    Compound compound = compoundStringCellDataFeatures.getValue();
                    String unit = compound.getUnit().toString();
                    return new SimpleStringProperty(unit);
                });
        ObservableList<String> observableUnitList = FXCollections.observableArrayList(Unit.mg.toString(),
                Unit.g.toString(), Unit.kg.toString(), Unit.ml.toString(), Unit.l.toString());
        unitCol.setCellFactory(ComboBoxTableCell.forTableColumn(observableUnitList));
        unitCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Compound, String> event) -> {
                    TablePosition<Compound, String> position = event.getTablePosition();
                    String newUnit = event.getNewValue();
                    int row = position.getRow();
                    Compound compound = event.getTableView().getItems().get(row);
                    if (!Unit.stringToEnum(newUnit).equals(compound.getUnit()))
                        saveChangeToBufferExecutor(compound, Field.UNIT, newUnit);
                });
        unitCol.setPrefWidth(Double.parseDouble(SecureProperties.getProperty("column.width.Unit")));
        boolean unit = "true".equals(SecureProperties.getProperty("column.show.Unit"));
        unitCol.setVisible(unit);
    }
}
