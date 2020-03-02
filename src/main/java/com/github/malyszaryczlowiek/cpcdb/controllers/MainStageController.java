package com.github.malyszaryczlowiek.cpcdb.controllers;

import com.github.malyszaryczlowiek.cpcdb.newBuffer.ActionType;
import com.github.malyszaryczlowiek.cpcdb.newBuffer.BufferExecutor;
import com.github.malyszaryczlowiek.cpcdb.tasks.LoadingDatabase;
import com.github.malyszaryczlowiek.cpcdb.tasks.LoadDatabaseFromRemoteWithoutMerging;
import com.github.malyszaryczlowiek.cpcdb.tasks.MergeRemoteDbWithLocal;
import com.github.malyszaryczlowiek.cpcdb.tasks.UpdateLocalDb;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.*;
import com.github.malyszaryczlowiek.cpcdb.alerts.*;
import com.github.malyszaryczlowiek.cpcdb.initializers.*;
import com.github.malyszaryczlowiek.cpcdb.managers.ColumnManager;
import com.github.malyszaryczlowiek.cpcdb.compound.*;
import com.github.malyszaryczlowiek.cpcdb.locks.LockProvider;
import com.github.malyszaryczlowiek.cpcdb.locks.LockTypes;
import com.github.malyszaryczlowiek.cpcdb.managers.CurrentStatusManager;
import com.github.malyszaryczlowiek.cpcdb.util.CloseProgramNotifier;
import com.github.malyszaryczlowiek.cpcdb.helperClasses.LaunchTimer;
import com.github.malyszaryczlowiek.cpcdb.util.SearchEngine;
import com.github.malyszaryczlowiek.cpcdb.properties.SecureProperties;
import com.github.malyszaryczlowiek.cpcdb.windows.windowLoaders.*;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


public class MainStageController implements Initializable,
        AddCompoundStageController.CompoundAddedListener, // Added live updating of TableView using
        SearchCompoundStageController.SearchingCriteriaChosenListener,
        EditCompoundStageController.EditChangesStageListener,
        AskToSaveChangesBeforeQuitController.SaveOrCancelListener,
        MergingRemoteDbController.Mergeable
{
    private Stage primaryStage;

    // table
    @FXML private TableView<Compound> mainSceneTableView;

    // table's columns
    //@FXML private TableColumn<Compound, Integer> idCol;
    @FXML private TableColumn<Compound, String> smilesCol;
    @FXML private TableColumn<Compound, String> compoundNumCol;
    @FXML private TableColumn<Compound, String> amountCol; // tu zmieniłem na String mimo, że normalenie powinno być float
    @FXML private TableColumn<Compound, String> unitCol;
    @FXML private TableColumn<Compound, String> formCol;
    @FXML private TableColumn<Compound, String> tempStabilityCol;
    @FXML private TableColumn<Compound, Boolean> argonCol;
    @FXML private TableColumn<Compound, String> containerCol;
    @FXML private TableColumn<Compound, String> storagePlaceCol;
    @FXML private TableColumn<Compound, LocalDateTime> lastModificationCol;
    @FXML private TableColumn<Compound, String> additionalInfoCol;

    // FILE ->
    //@FXML private Menu menuFile;

    @FXML private MenuItem menuFileAddCompound;
    @FXML private MenuItem menuFileSearch;
    @FXML private MenuItem menuFileLoadFullTable;
    @FXML private MenuItem menuFileLoadDataFromRemoteServer;
    @FXML private MenuItem menuFileSave;
    @FXML private MenuItem menuFilePreferences;
    @FXML private MenuItem menuFileQuit;

    // Edit ->
    //@FXML private Menu menuEdit;

    @FXML private MenuItem menuEditSelectedCompound;
    @FXML private MenuItem menuEditDeleteSelectedCompounds;

    // View -> Full Screen
    @FXML private CheckMenuItem menuViewFullScreen;
    @FXML private MenuItem menuViewShowAllColumns;

    // Help -> About CPCDB
    @FXML private MenuItem menuHelpAboutCPCDB;

    @FXML private MenuItem menuEditUndo;
    @FXML private MenuItem menuEditRedo;

    // Context menu of table
    @FXML private MenuItem editSelectedCompoundContext;
    @FXML private MenuItem deleteSelectedCompoundsContext;
    @FXML private MenuItem showAllColumnsContext;


    @FXML private ProgressBar progressBar;
    @FXML private Text currentStatus;

    private DoubleProperty progressValue;
    private boolean unblockingTask3 = false;
    private BufferExecutor bufferExecutor;
    private ColumnManager columnManager;
    private List<Compound> fullListOfCompounds; // to jest lista którą trzeba używać jak chcemy zapisać dane do bazy danych
    private ObservableList<Compound> observableList;
    private LaunchTimer initializationTimer;

    private LockProvider lockProvider;
    private CurrentStatusManager currentStatusManager;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializationTimer = new LaunchTimer();

        if (!(new File("propertiesFile").exists())) // checkIfAskForDBProperties()
            WindowFactory.showWindow(WindowsEnum.INITIALIZING_DB_PROPERTIES_WINDOW, null, null);
        else {
            LaunchTimer keyAndPropertiesLoading = new LaunchTimer();
            SecureProperties.loadProperties();
            keyAndPropertiesLoading.stopTimer("key and properties loading time: ");
            CloseProgramNotifier.setToNotCloseProgram();
        }

        if ( ! CloseProgramNotifier.getIfCloseUninitializedProgram() ) {
            progressValue = new SimpleDoubleProperty(0.0);
            currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager(currentStatus);
            currentStatusManager.setCurrentStatus("Initializing program");
            lockProvider = LockProvider.getLockProvider();
            fullListOfCompounds = new ArrayList<>();
            observableList = FXCollections.observableArrayList(fullListOfCompounds); // todo TE DWIE LINIJKI MOŻNA chyba przenieść do loadingdatabaseTask  12 linijek niżej
            mainSceneTableView.setItems(observableList);
            bufferExecutor = BufferExecutor.getBufferExecutor(mainSceneTableView, observableList,
                    menuEditUndo, menuEditRedo, menuFileSave);
            columnManager = new ColumnManager( new MenuItem[]{menuViewShowAllColumns, showAllColumnsContext},
                    smilesCol, compoundNumCol, amountCol, unitCol, formCol, tempStabilityCol,
                    argonCol, containerCol, storagePlaceCol, lastModificationCol, additionalInfoCol);
            ColumnInitializer.setUpInitializer(mainSceneTableView, bufferExecutor, observableList);
            ErrorFlagsManager.initializeErrorFlagsManager();
            if ( !SecureProperties.hasProperty("column.width.Smiles") ) setWidthOfColumns();

            Task<String> loadingDatabaseTask = LoadingDatabase.getTask(progressValue,fullListOfCompounds,
                    observableList, mainSceneTableView, progressBar, currentStatusManager, this);
            Thread loadingDatabaseThread = new Thread(loadingDatabaseTask);
            loadingDatabaseThread.setDaemon(true);
            loadingDatabaseThread.start();
            Task<Void> task1 = new Task<>()
            {
                @Override
                protected Void call() {
                    settingUpTask1();
                    return null;
                }
            };
            Task<Void> task2 = new Task<>()
            {
                @Override
                protected Void call() {
                    settingUpTask2();
                    return null;
                }
            };
            Task<Void> task3 = new Task<>()
            {
                @Override
                protected Void call() {
                    settingUpTask3();
                    return null;
                }
            };
            Thread threadTask1 = new Thread(task1);
            Thread threadTask2 = new Thread(task2);
            Thread threadTask3 = new Thread(task3);

            threadTask1.setDaemon(true);
            threadTask2.setDaemon(true);
            threadTask3.setDaemon(true);

            threadTask1.start();
            threadTask2.start();
            threadTask3.start();
        }
    }

    /*
     * ###############################################
     * FUNCTIONS FOR SETTING UP STAGE AND HIS COMPONENTS
     * ###############################################
     */

    public void setStage(Stage stage) {
        primaryStage = stage;
        if ( CloseProgramNotifier.getIfCloseUninitializedProgram() ) {
            primaryStage.close();
            Platform.exit();
        } else {
            primaryStage.show();
            primaryStage.setOnCloseRequest(
                    windowEvent -> {
                        windowEvent.consume();
                        closeProgram();
                    });
            mainSceneTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            mainSceneTableView.setOnContextMenuRequested(
                    contextMenuEvent -> {
                        int count = mainSceneTableView.getSelectionModel().getSelectedItems().size();
                        if ( count == 1 ) editSelectedCompoundContext.setDisable(false); // Edit Selected Compound
                        else editSelectedCompoundContext.setDisable(true);
                        if ( count >= 1 ) deleteSelectedCompoundsContext.setDisable(false);//Delete Selected Compound(s)
                        else deleteSelectedCompoundsContext.setDisable(true);
                    });
        }
        synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
            progressValue.setValue( progressValue.get() + 0.3);
        }
        initializationTimer.stopTimer("initializing process completed");
    }

    private void settingUpTask1() {
        new SmilesColumnInitializer(smilesCol).initialize();
        new CompoundNumberInitializer(compoundNumCol).initialize();
        new AmountColumnInitializer(amountCol).initialize();
        new UnitColumnInitializer(unitCol).initialize();
        MenuItemInitializer.initialize(menuFileAddCompound, menuFileLoadFullTable, menuFileSave,
                menuFileSearch,menuFilePreferences, menuFileQuit, menuEditSelectedCompound,
                menuViewFullScreen, menuHelpAboutCPCDB, menuFileLoadDataFromRemoteServer);
        menuViewFullScreen.setSelected(false);
        synchronized (lockProvider.getLock(LockTypes.INITIALIZATION_END)) {
            if (!unblockingTask3)
                unblockingTask3 = true;
            else
                lockProvider.getLock(LockTypes.INITIALIZATION_END).notifyAll();
        }
    }

    private void settingUpTask2() {
        new FormColumnInitializer(formCol).initialize();
        new TemperatureStabilityColumnInitializer(tempStabilityCol).initialize();
        new ArgonColumnInitializer(argonCol).initialize();
        new ContainerColumnInitializer(containerCol).initialize();
        synchronized (lockProvider.getLock(LockTypes.INITIALIZATION_END)) {
            if (!unblockingTask3)
                unblockingTask3 = true;
            else
                lockProvider.getLock(LockTypes.INITIALIZATION_END).notifyAll();
        }
    }

    private void settingUpTask3() {
        new StoragePlaceColumnInitializer(storagePlaceCol).initialize();
        new LastModificationColumnInitializer(lastModificationCol).initialize();
        new AdditionalInfoColumnInitializer(additionalInfoCol).initialize();
        try {
            synchronized (lockProvider.getLock(LockTypes.INITIALIZATION_END)) {
                lockProvider.getLock(LockTypes.INITIALIZATION_END).wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean areAllColumnsVisible = columnManager.areAllColumnsVisible();
        menuViewShowAllColumns.setDisable(areAllColumnsVisible);
        showAllColumnsContext.setDisable(areAllColumnsVisible);
        columnManager.setColumnFontSize();
    }

    /*
    * ###############################################
    * FUNCTIONS FROM MENUS ITEMS
    * ###############################################
    */

   // FILE -> Add Compound
    @FXML
    protected void menuFileAddCompound(ActionEvent event) {
        WindowFactory.showWindow(WindowsEnum.ADD_COMPOUND_WINDOW, this, null);
        event.consume();
    }

    // FILE -> Search
    @FXML
    protected void onFileSearchMenuItemClicked(ActionEvent actionEvent) {
        WindowFactory.showWindow(WindowsEnum.SEARCH_COMPOUND_WINDOW, null, null);
        actionEvent.consume();
    }

    // FILE -> Save
    /**
     * metoda uruchamiana po kliknięciu save w menu programu
     */
    @FXML
    protected void onMenuFileSaveClicked() {
        if ( bufferExecutor.returnCurrentIndex() > 0 )
            bufferExecutor.saveChangesToDatabase();  // robię save
        // TODO tutaj trzeba uruchomić oddzielny task do zapisania nowych zmian bo każde wywołanie zapytania do bazy danych
        // powinno być wykonane w oddzielnym wątku tak aby nie blokować głównego.
        // TODO ale powinno używać locka tak aby wątek główny nie został zamknięty zbyt szybko
    }

    @FXML
    protected void onMenuFilePreferencesClicked(ActionEvent event) {
        WindowFactory.showWindow(WindowsEnum.SETTINGS_WINDOW,null, null);
        event.consume();
    }

    @FXML
    protected void onMenuFileLoadDataFromRemoteServer() {
        // to jakoś trzeba zimplementować
        // TODO sprawdź czy sa wprowadzone jakieś zmiany czy bufor nie jest pusty
        // jeśli jest to wyślij zmiany do remote server i wczytaj ponownie całą bazę.

        /*
        if (brak błędów połączenia do remote server) {
            fullListOfCompounds.clear();
            observableList.clear();
            Task<String> loadingDatabaseTask = LoadingDatabase.getTask(progressValue,fullListOfCompounds,
                    observableList, mainSceneTableView, progressBar, currentStatusManager, this);
            Thread loadingDatabaseThread = new Thread(loadingDatabaseTask);
            loadingDatabaseThread.setDaemon(true);
            loadingDatabaseThread.start();
        }
         */
    }

    // FILE -> Quit

    @FXML
    protected void onMenuFileQuit() {
        closeProgram();
    }

    // EDIT

    @FXML
    protected void menuEdit() {
        int count = mainSceneTableView.getSelectionModel().getSelectedItems().size();
        // Edit -> Edit Selected Compound
        if (count == 1) menuEditSelectedCompound.setDisable(false);
        else menuEditSelectedCompound.setDisable(true);
        // Edit -> Delete Selected Compound(s)
        if (count >= 1) menuEditDeleteSelectedCompounds.setDisable(false);
        else menuEditDeleteSelectedCompounds.setDisable(true);
    }

        /*  to jest przeniesione do bufferExeutora
        // Edit -> Undo
        if ( bufferExecutor.returnCurrentIndex() > 0 ) menuEditUndo.setDisable(false);
        else menuEditUndo.setDisable(true);

        // Edit -> Redo
        if ( bufferExecutor.isNothingToRedo() ) menuEditRedo.setDisable(false);
        else menuEditRedo.setDisable(true);
        */
    // EDIT -> Undo

    @FXML
    protected void onUndoClicked() {
        bufferExecutor.undo();
    } // robię undo

    @FXML
    protected void onRedoClicked() {
        bufferExecutor.redo();
    } // robię redo

    @FXML
    protected void onMenuEditSelectAll() {
        mainSceneTableView.getSelectionModel().selectAll();
    }

    // VIEW -> Full Screen

    @FXML
    protected void changeFullScreenMode(ActionEvent event) {
        if (primaryStage.isFullScreen()) {
            primaryStage.setFullScreen(false);
            menuViewFullScreen.setSelected(false);
        }
        else {
            primaryStage.setFullScreen(true);
            menuViewFullScreen.setSelected(true);
        }
        event.consume();
    }

    // VIEW -> SHOW -> Show All Columns

    @FXML
    protected void onMenuShowAllColumns() {
        columnManager.onShowHideAllColumns();
    }


    @FXML
    protected void showEditCompoundStage() {
        Compound selectedItems = mainSceneTableView.getSelectionModel()
                .getSelectedItems().get(0);
        WindowFactory.showWindow(WindowsEnum.EDIT_COMPOUND_WINDOW, this, selectedItems);
    }


    @Override
    public void notifyAboutAddedCompound(Compound compound) {
        observableList.add(compound);
        Integer index = observableList.indexOf(compound);
        Map<Integer, Compound> toInsert = new TreeMap<>();
        toInsert.put(index, compound);
        bufferExecutor.addChange(ActionType.INSERT, toInsert,null,null); //  robię insert
    }


    /**
     * Metoda wywołana z interfejsu
     * SearchCompoundStageController.OnChosenSearchingCriteriaListener
     * która ma za zadanie odfiltrowanie compoundów spełniających żadane kryteria
     * a następnie umieszczenie znalezionych związków w tabeli
     */
    @Override
    public void searchingCriteriaChosen(String smiles, String smilesAccuracy,
                                        String compoundNumber, String compoundNumberAccuracy,
                                        String form, String container, String storagePlace,
                                        String beforeAfter, LocalDate selectedLocalDate,
                                        String argon, String temperature, String additionalInfo)
    {
        SearchEngine searchEngine = new SearchEngine(fullListOfCompounds, smiles, smilesAccuracy,
                compoundNumber, compoundNumberAccuracy, form, container, storagePlace, beforeAfter,
                selectedLocalDate, argon, temperature, additionalInfo);
        if (!searchEngine.returnListOfFoundCompounds().isEmpty()) {// display found compounds
            observableList.clear();
            observableList.setAll( searchEngine.returnListOfFoundCompounds() );
            mainSceneTableView.refresh();
        }
        else ShortAlertWindowFactory.showErrorWindow(ErrorType.NOT_FOUND_COMPOUND);
    }

    @FXML
    protected void reloadTable() {
        observableList.clear();
        observableList.setAll(fullListOfCompounds);
        mainSceneTableView.refresh();
    }

    /*
     * ###############################################
     * METHODS OF TABLE VIEW CONTEXT MENU
     * ###############################################
     */

    @FXML
    protected void deleteSelectedCompounds(ActionEvent event) {
        ObservableList<Compound> selectedItems = mainSceneTableView.getSelectionModel().getSelectedItems();
        Map<Integer, Compound> mapOfCompounds = new TreeMap<>();
        selectedItems.forEach( compound -> mapOfCompounds.put( observableList.indexOf( compound ), compound ) );
        bufferExecutor.addChange(ActionType.REMOVE, mapOfCompounds, null, null);// robię remove
        fullListOfCompounds.clear();
        fullListOfCompounds.addAll(observableList.sorted());
        event.consume();
    }

    /*
     * ###############################################
     * METHODS TO CLOSE PROGRAM
     * ###############################################
     */

    private void closeProgram() {
        if ( bufferExecutor.returnCurrentIndex() > 0 )
            WindowFactory.showWindow(WindowsEnum.SAVE_CHANGES_BEFORE_QUIT_WINDOW,this, null);
        else onCloseProgramWithoutChanges();
    }

    @Override
    public void reloadTableAfterCompoundEdition() { mainSceneTableView.refresh(); }

    /**
     * Method implemented from EditCompoundStageController.EditChangesStageListener interface
     * @param compound Compound to delete
     */
    @Override
    public void reloadTableAfterCompoundDeleting(Compound compound) {
        Map<Integer, Compound> compoundToDelete = new TreeMap<>();
        var index =  observableList.indexOf(compound);
        compoundToDelete.put(index, compound );
        bufferExecutor.addChange(ActionType.REMOVE, compoundToDelete, null, null); // robię remove
        //observableList.remove(compound);
        //mainSceneTableView.refresh();
    }

    @Override
    public void onSaveChangesAndCloseProgram() {
        LaunchTimer closeTimer = new LaunchTimer();
        Task<Void> task1 = new Task<>()
        {
            @Override
            protected Void call()
            {
                LaunchTimer saveTimer = new LaunchTimer();
                bufferExecutor.saveChangesToDatabase(); // robię save
                saveTimer.stopTimer("Save Timer is stopped");
                return null;
            }
        };
        Task<Void> task2 = new Task<>()
        {
            @Override
            protected Void call() {
                setWidthOfColumns();
                SecureProperties.saveProperties();
                primaryStage.close();
                return null;
            }
        };
        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);
        thread1.start();
        thread2.start();
        try {
            thread2.join();
            thread1.join();
        }
        catch (InterruptedException e) { e.printStackTrace(); }
        closeTimer.stopTimer("closing time when must save data to DB");
        Platform.exit();
    }

    @Override
    public void onCloseProgramWithoutChanges() {
        LaunchTimer closeTimer = new LaunchTimer();
        //saveTableViewsColumnSizesAndOrder();
        setWidthOfColumns();
        SecureProperties.saveProperties();
        closeTimer.stopTimer("closing time when NOT saving changes only properties");
        Platform.exit();
    }

    private void setWidthOfColumns() {
        SecureProperties.setProperty("column.width.Smiles", String.valueOf( smilesCol.getWidth() ));
        SecureProperties.setProperty("column.width.CompoundName", String.valueOf( compoundNumCol.getWidth() ));
        SecureProperties.setProperty("column.width.Amount", String.valueOf( amountCol.getWidth() ));
        SecureProperties.setProperty("column.width.Unit", String.valueOf( unitCol.getWidth() ));
        SecureProperties.setProperty("column.width.Form", String.valueOf( formCol.getWidth() ));
        SecureProperties.setProperty("column.width.TemperatureStability", String.valueOf( tempStabilityCol.getWidth() ));
        SecureProperties.setProperty("column.width.Argon", String.valueOf( argonCol.getWidth() ));
        SecureProperties.setProperty("column.width.Container", String.valueOf( containerCol.getWidth() ));
        SecureProperties.setProperty("column.width.StoragePlace", String.valueOf( storagePlaceCol.getWidth() ));
        SecureProperties.setProperty("column.width.LastModification", String.valueOf( lastModificationCol.getWidth() ));
        SecureProperties.setProperty("column.width.AdditionalInfo", String.valueOf( additionalInfoCol.getWidth() ));
    }

    @FXML
    protected void onShowHideColumn() {
        WindowFactory.showWindow(WindowsEnum.COLUMN_MANAGER_WINDOW, null, null);
    }

    @FXML
    private void onCurrentStatusTextClicked() {
        String text = currentStatus.getText();
        if ( text.contains("Warning") || text.contains("Error") ) {
            if ( ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR)
                    && ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR) )
                ShortAlertWindowFactory.showErrorWindow(ErrorType.CANNOT_CONNECT_TO_ALL_DB);

            else if ( ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR)
                    && ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR) )
                ShortAlertWindowFactory.showErrorWindow(ErrorType.CANNOT_CONNECT_TO_REMOTE_BD_AND_INCORRECT_LOCAL_PASSPHRASE);


            else if ( ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR)
                    && !ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR)
                    && !ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR) )
                ShortAlertWindowFactory.showErrorWindow(ErrorType.CANNOT_CONNECT_TO_REMOTE_DB);


            else if ( ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR)
                    && ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR) )
                ShortAlertWindowFactory.showErrorWindow(ErrorType.INCORRECT_REMOTE_PASSPHRASE_AND_CANNOT_CONNECT_TO_LOCAL_DB);


            else if ( ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR)
                    && ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR) )
                ShortAlertWindowFactory.showErrorWindow(ErrorType.INCORRECT_REMOTE_AND_LOCAL_PASSPHRASE);


            else if ( ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR)
                    && !ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR)
                    && !ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR) ) {
                ShortAlertWindowFactory.showErrorWindow(ErrorType.INCORRECT_REMOTE_PASSPHRASE);
            }
        }
        //if (text.equals("Connection to Remote Database Established"))
        //    WindowFactory.showWindow(WindowsEnum.MERGING_REMOTE_DB_WINDOW,this,null);
        currentStatusManager.resetFont();
    }

    /**
     * From interface MergingRemoteDbController.Mergeable
     */
    @Override
    public void mergeWithRemote() {
        Task<Void> mergingTask = MergeRemoteDbWithLocal.getTask(progressValue);
        mergingTask.messageProperty().addListener( (observable, oldMessage, newMessage) ->
                currentStatusManager.setCurrentStatus(newMessage)
        );
        mergingTask.progressProperty().addListener((observable, oldValue, newValue) ->
                progressBar.setProgress((Double) newValue)
        );
        Thread mergingThread = new Thread(mergingTask);
        mergingThread.setDaemon(true);
        mergingThread.start();
    }

    /**
     * From interface MergingRemoteDbController.Mergeable
     */
    @Override
    public void loadFromRemoteWithoutMerging() {
        Task<Void> mergingTask = LoadDatabaseFromRemoteWithoutMerging.getTask(progressValue, fullListOfCompounds,
                observableList, mainSceneTableView);
        mergingTask.messageProperty().addListener( (observable, oldMessage, newMessage) ->
                currentStatusManager.setCurrentStatus(newMessage)
        );
        mergingTask.progressProperty().addListener((observable, oldValue, newValue) ->
                progressBar.setProgress((Double) newValue)
        );
        mergingTask.titleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("Reloading Remote Server Database Done")){
                currentStatusManager.setCurrentStatus("Reloading Remote Server Database Done");
                Task<Void> updatingTask = UpdateLocalDb.getTask(observableList);
                Thread updatingLocalDbThread = new Thread(updatingTask);
                updatingLocalDbThread.setDaemon(true);
                updatingLocalDbThread.start();
            }
        });
        Thread mergingThread = new Thread(mergingTask);
        mergingThread.setDaemon(true);
        mergingThread.start();
    }
}





