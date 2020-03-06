package com.github.malyszaryczlowiek.cpcdb.managers.initializers;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

public class StoragePlaceColumnInitializer  extends ColumnInitializer implements Initializable
{
    private TableColumn<Compound, String> storagePlaceCol;

    public StoragePlaceColumnInitializer(TableColumn<Compound, String> storagePlaceCol){
        this.storagePlaceCol = storagePlaceCol;
    }

    @Override
    public void initialize() {
        storagePlaceCol.setCellValueFactory(new PropertyValueFactory<>("storagePlace"));
        storagePlaceCol.setCellFactory(TextFieldTableCell.forTableColumn());
        storagePlaceCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Compound, String> event) -> {
                    TablePosition<Compound, String> position = event.getTablePosition();
                    String newStoragePlace = event.getNewValue();
                    int row = position.getRow();
                    Compound compound = event.getTableView().getItems().get(row);
                    if ( !newStoragePlace.equals( compound.getStoragePlace() ) )
                        saveChangeToBufferExecutor(compound, Field.STORAGEPLACE, newStoragePlace);
                });
        storagePlaceCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.StoragePlace") ));
        boolean storagePlace = "true".equals(SecureProperties.getProperty("column.show.StoragePlace"));
        storagePlaceCol.setVisible(storagePlace);
    }
}
