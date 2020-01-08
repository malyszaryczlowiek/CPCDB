package com.github.malyszaryczlowiek.cpcdb.initializers;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.io.IOException;

public class AdditionalInfoColumnInitializer extends ColumnInitializer implements Initializable
{
    private TableColumn<Compound, String> additionalInfoCol;

    public AdditionalInfoColumnInitializer(TableColumn<Compound, String> additionalInfoCol) {
        this.additionalInfoCol = additionalInfoCol;
    }

    @Override
    public void initialize() {
        additionalInfoCol.setCellValueFactory(new PropertyValueFactory<>("additionalInfo"));
        additionalInfoCol.setCellFactory(TextFieldTableCell.forTableColumn());
        additionalInfoCol.setOnEditCommit(
                ( TableColumn.CellEditEvent<Compound, String> event ) -> {
                    TablePosition<Compound, String> position = event.getTablePosition();
                    String newInfo = event.getNewValue();
                    int row = position.getRow();

                    Compound compound = event.getTableView().getItems().get(row);

                    if (!newInfo.equals(compound.getAdditionalInfo())) {
                        try {
                            changesDetector.makeEdit(compound, Field.ADDITIONALINFO, newInfo);
                            mainSceneTableView.refresh();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        additionalInfoCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.AdditionalInfo") ));
        boolean additionalInfo =  "true".equals(SecureProperties.getProperty("column.show.AdditionalInfo"));
        additionalInfoCol.setVisible(additionalInfo);
    }
}
