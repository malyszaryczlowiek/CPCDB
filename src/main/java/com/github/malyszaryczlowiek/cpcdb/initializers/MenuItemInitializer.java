package com.github.malyszaryczlowiek.cpcdb.initializers;

import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;

public class MenuItemInitializer
{
    public static void initialize(MenuItem... menuItems) {
        menuItems[0].setAccelerator(KeyCombination.keyCombination("Ctrl+I")); // i from insert menuFileAddCompound
        menuItems[1].setAccelerator(KeyCombination.keyCombination("Ctrl+R")); // R from reload menuFileLoadFullTable
        menuItems[2].setAccelerator(KeyCombination.keyCombination("Ctrl+S")); // S from save; menuFileSave.
        menuItems[3].setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+S")); // S from search ;
        menuItems[4].setAccelerator(KeyCombination.keyCombination("Ctrl+P")); // P from preferences ;
        menuItems[5].setAccelerator(KeyCombination.keyCombination("Ctrl+Q")); // q from quit ;
        menuItems[6].setAccelerator(KeyCombination.keyCombination("Ctrl+E"));
        menuItems[7].setAccelerator(KeyCombination.keyCombination("Ctrl+F"));
        menuItems[8].setAccelerator(KeyCombination.keyCombination("Ctrl+H"));
        menuItems[9].setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+R"));
        //menuEditUndo.setAccelerator(KeyCombination.keyCombination("Ctrl+U"));
        //menuEditRedo.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
    }
}
