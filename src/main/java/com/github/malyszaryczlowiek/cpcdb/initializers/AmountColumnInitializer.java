package com.github.malyszaryczlowiek.cpcdb.initializers;

import com.github.malyszaryczlowiek.cpcdb.alertWindows.FloatNumberFormatError;
import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.cell.TextFieldTableCell;

import java.io.IOException;

public class AmountColumnInitializer extends ColumnInitializer implements Initializable
{
    private TableColumn<Compound, String> amountCol;

    public AmountColumnInitializer(TableColumn<Compound, String> amountCol) {
        this.amountCol = amountCol;
    }

    @Override
    public void initialize() {
        amountCol.setCellValueFactory(
                (TableColumn.CellDataFeatures<Compound, String> compoundFloatCellDataFeatures) -> {
                    Float f = compoundFloatCellDataFeatures.getValue().getAmount();
                    String s = String.valueOf(f);
                    return new SimpleStringProperty(s);
                });
        amountCol.setCellFactory(TextFieldTableCell.forTableColumn());
        amountCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Compound, String> event) -> {
                    TablePosition<Compound, String> position = event.getTablePosition();
                    String newValue = event.getNewValue();
                    int row = position.getRow();
                    Compound compound = event.getTableView().getItems().get(row);
                    Float f;
                    try {
                        f = Float.valueOf(newValue);
                        if (!f.equals(compound.getAmount())) {
                            try {
                                changesDetector.makeEdit(compound, Field.AMOUNT, f);
                                mainSceneTableView.refresh();
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    catch (NumberFormatException e) {
                        e.printStackTrace();
                        new FloatNumberFormatError(Alert.AlertType.ERROR).show();
                    }
                });
        amountCol.setPrefWidth(Double.parseDouble(SecureProperties.getProperty("column.width.Amount")));
        boolean amount = "true".equals(SecureProperties.getProperty("column.show.Amount"));
        amountCol.setVisible(amount);
    }
}
