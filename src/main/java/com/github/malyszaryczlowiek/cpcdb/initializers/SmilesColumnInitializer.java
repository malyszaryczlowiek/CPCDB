package com.github.malyszaryczlowiek.cpcdb.initializers;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.io.IOException;

public  class  SmilesColumnInitializer extends ColumnInitializer implements Initializable
{
    private TableColumn<Compound, String> smilesCol;

    public SmilesColumnInitializer(TableColumn<Compound, String> smilesCol) {
        this.smilesCol = smilesCol;
    }

    @Override
    public void initialize() {
        smilesCol.setCellValueFactory(new PropertyValueFactory<>("smiles"));
        smilesCol.setCellFactory(TextFieldTableCell.forTableColumn());
        smilesCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Compound, String> event) -> {
                    TablePosition<Compound, String> pos = event.getTablePosition();
                    String newSmiles = event.getNewValue();
                    int row = pos.getRow();

                    Compound compound = event.getTableView().getItems().get(row);

                    if (!newSmiles.equals(compound.getSmiles())) {
                        try {
                            changesDetector.makeEdit(compound, Field.SMILES, newSmiles);
                            mainSceneTableView.refresh();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        smilesCol.setPrefWidth(Double.parseDouble(SecureProperties.getProperty("column.width.Smiles")));
        boolean smiles= "true".equals(SecureProperties.getProperty("column.show.Smiles"));
        smilesCol.setVisible(smiles);
    }
}
