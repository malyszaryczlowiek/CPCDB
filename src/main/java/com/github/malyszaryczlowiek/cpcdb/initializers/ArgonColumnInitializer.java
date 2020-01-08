package com.github.malyszaryczlowiek.cpcdb.initializers;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.io.IOException;

public class ArgonColumnInitializer extends ColumnInitializer implements Initializable
{
    private TableColumn<Compound, Boolean> argonCol;

    public ArgonColumnInitializer(TableColumn<Compound, Boolean> argonCol) {
        this.argonCol = argonCol;
    }

    @Override
    public void initialize() {
        // Argon column Set up
        argonCol.setCellValueFactory(
                (TableColumn.CellDataFeatures<Compound, Boolean> compoundBooleanCellDataFeatures) -> {
                    Compound compound = compoundBooleanCellDataFeatures.getValue();
                    SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty(compound.isArgon());
                    booleanProperty.addListener(
                            (ObservableValue<? extends Boolean> observableValue,
                             Boolean oldValue, Boolean newValue) -> {
                                try {
                                    changesDetector.makeEdit(compound, Field.ARGON, newValue);
                                    mainSceneTableView.refresh();
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } );

                    return booleanProperty;
                } );
        argonCol.setCellFactory(
                (TableColumn<Compound, Boolean> compoundBooleanTableColumn) -> {
                    CheckBoxTableCell<Compound, Boolean> cell = new CheckBoxTableCell<>();
                    cell.setAlignment(Pos.CENTER);
                    return cell;
                } );
        argonCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.Argon") ));
        boolean argon =  "true".equals(SecureProperties.getProperty("column.show.Argon"));
        argonCol.setVisible( argon );
    }
}
