package com.github.malyszaryczlowiek.cpcdb.initializers;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.io.IOException;

public class FormColumnInitializer extends ColumnInitializer implements Initializable
{
    private TableColumn<Compound, String> formCol;

    public FormColumnInitializer(TableColumn<Compound, String> formCol) {
        this.formCol = formCol;
    }

    @Override
    public void initialize() {
        formCol.setCellValueFactory( new PropertyValueFactory<>("form") );
        formCol.setCellFactory( TextFieldTableCell.forTableColumn() );
        formCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Compound, String> event) -> {
                    TablePosition<Compound, String> position = event.getTablePosition();
                    String newForm = event.getNewValue();
                    int row = position.getRow();
                    Compound compound = event.getTableView().getItems().get(row);
                    if ( !newForm.equals(compound.getForm()) ) {
                        try {
                            changesDetector.makeEdit(compound, Field.FORM, newForm);
                            mainSceneTableView.refresh();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        formCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.Form") ));
        boolean form = "true".equals(SecureProperties.getProperty("column.show.Form")) ;
        formCol.setVisible(form);
    }
}
