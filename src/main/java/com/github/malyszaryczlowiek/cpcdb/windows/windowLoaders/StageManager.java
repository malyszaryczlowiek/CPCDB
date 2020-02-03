package com.github.malyszaryczlowiek.cpcdb.windows.windowLoaders;

import javafx.stage.Stage;

import java.util.concurrent.ConcurrentHashMap;

public class StageManager
{
    private static ConcurrentHashMap<WindowsEnum, Stage> mapOfOpenStages;
    private static StageManager stageManager;

    public static StageManager getStageManager() {
        if (mapOfOpenStages == null) {
            stageManager = new StageManager();
            mapOfOpenStages = new ConcurrentHashMap<>();
        }
        return stageManager;
    }

    private StageManager() {}

    public void addStage(WindowsEnum windowsEnum, Stage stage) {
        mapOfOpenStages.putIfAbsent(windowsEnum, stage);
    }

    public void removeStage(WindowsEnum windowsEnum) {
        mapOfOpenStages.remove(windowsEnum);
    }

    public boolean isAnyWindowOpen() {
        return !mapOfOpenStages.isEmpty();
    }

    public void closeAllWindows() {
        if (isAnyWindowOpen())
            mapOfOpenStages.values().forEach( Stage::close );
    }
}

/*
TODO
tego managera trzeba użyć w przypadku gdy uruchamiamy edycję jakiegoś compounda , wyszukiwanie
i ten manager musi zamykać wszystkie otworzone okna w momencie gdy odzyskujemy połączenie ze zdalnym serwerem
 */
