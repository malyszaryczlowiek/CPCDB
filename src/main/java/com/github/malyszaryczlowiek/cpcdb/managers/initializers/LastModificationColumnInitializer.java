package com.github.malyszaryczlowiek.cpcdb.managers.initializers;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;

public class LastModificationColumnInitializer extends ColumnInitializer implements Initializable
{
    private TableColumn<Compound, LocalDateTime> lastModificationCol;

    public LastModificationColumnInitializer(TableColumn<Compound, LocalDateTime> lastModificationCol) {
        this.lastModificationCol = lastModificationCol;
    }

    @Override
    public void initialize() {
        // Last Modification column set Up
        lastModificationCol.setCellValueFactory(new PropertyValueFactory<>("dateTimeModification"));
        lastModificationCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.LastModification") ));
        boolean isColumnVisible = "true".equals(SecureProperties.getProperty("column.show.LastModification"));
        lastModificationCol.setVisible(isColumnVisible);
    }
}
