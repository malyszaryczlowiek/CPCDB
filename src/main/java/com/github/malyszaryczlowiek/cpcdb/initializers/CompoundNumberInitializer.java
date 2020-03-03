package com.github.malyszaryczlowiek.cpcdb.initializers;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

public class CompoundNumberInitializer extends ColumnInitializer implements Initializable
{
    private TableColumn<Compound, String> compoundNumCol;

    public CompoundNumberInitializer(TableColumn<Compound, String> compoundNum){
        this.compoundNumCol = compoundNum;
    }

    @Override
    public void initialize() {
        // Compound Number column set up
        compoundNumCol.setCellValueFactory(new PropertyValueFactory<>("compoundNumber"));
        compoundNumCol.setCellFactory( TextFieldTableCell.forTableColumn() );
        compoundNumCol.setOnEditCommit(
                ( TableColumn.CellEditEvent<Compound, String> event ) -> {
                    TablePosition<Compound, String> position = event.getTablePosition();
                    String newNumber = event.getNewValue();
                    int row = position.getRow();
                    Compound compound = event.getTableView().getItems().get(row);
                    if (!newNumber.equals(compound.getCompoundNumber()))
                        saveChangeToBufferExecutor(compound, Field.COMPOUNDNUMBER, newNumber);
                });
        compoundNumCol.setPrefWidth(Double.parseDouble(SecureProperties.getProperty("column.width.CompoundName")));
        boolean compoundNumber = "true".equals(SecureProperties.getProperty("column.show.CompoundName"));
        compoundNumCol.setVisible(compoundNumber);
    }
}
