package com.github.malyszaryczlowiek.cpcdb.windows.windowLoaders;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.controllers.MainStageController;

import java.io.IOException;

public class WindowFactory
{
    public static void showWindow(WindowsEnum windowsEnum, MainStageController mainStageController, Compound compoundToEdition ) {
        try {
            switch (windowsEnum) {
                case ADD_COMPOUND_WINDOW:
                    new AddCompoundWindow(mainStageController).show();
                    break;
                case COLUMN_MANAGER_WINDOW:
                    new ColumnManagerWindow().show();
                    break;
                case EDIT_COMPOUND_WINDOW:
                    new EditCompoundWindow(mainStageController, compoundToEdition).show();
                    break;
                case INITIALIZING_DB_PROPERTIES_WINDOW:
                    new InitializingDBPropertiesWindow().show();
                    break;
                case SAVE_CHANGES_BEFORE_QUIT_WINDOW:
                    new SaveChangesBeforeQuitWindow(mainStageController).show();
                    break;
                case SEARCH_COMPOUND_WINDOW:
                    new SearchCompoundWindow(mainStageController).show();
                    break;
                case SETTINGS_WINDOW:
                    new SettingsWindow().show();
                    break;
                case MERGING_REMOTE_DB_WINDOW:
                    new MergingRemoteDbWindow(mainStageController).show();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
