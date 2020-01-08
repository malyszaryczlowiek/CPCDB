package com.github.malyszaryczlowiek.cpcdb.initializers;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.io.IOException;

public class ContainerColumnInitializer extends ColumnInitializer implements Initializable
{
    private  TableColumn<Compound, String> containerCol;

    public ContainerColumnInitializer(TableColumn<Compound, String> containerCol) {
        this.containerCol = containerCol;
    }

    @Override
    public void initialize() {
        // Container column set Up
        containerCol.setCellValueFactory(new PropertyValueFactory<>("container"));
        containerCol.setCellFactory(TextFieldTableCell.forTableColumn());
        containerCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Compound, String> event) -> {
                    TablePosition<Compound, String> position = event.getTablePosition();
                    String newContainer = event.getNewValue();
                    int row = position.getRow();
                    Compound compound = event.getTableView().getItems().get(row);
                    if (!newContainer.equals(compound.getContainer())) {
                        try {
                            changesDetector.makeEdit(compound, Field.CONTAINER, newContainer);
                            mainSceneTableView.refresh();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        containerCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.Container") ));
        boolean container =  "true".equals(SecureProperties.getProperty("column.show.Container"));
        containerCol.setVisible( container );
    }
}
