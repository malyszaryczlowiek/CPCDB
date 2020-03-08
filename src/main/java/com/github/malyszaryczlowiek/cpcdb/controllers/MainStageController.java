package com.github.malyszaryczlowiek.cpcdb.controllers;

import com.github.malyszaryczlowiek.cpcdb.buffer.ActionType;
import com.github.malyszaryczlowiek.cpcdb.buffer.BufferExecutor;
import com.github.malyszaryczlowiek.cpcdb.tasks.LoadRemoteDatabase;
import com.github.malyszaryczlowiek.cpcdb.tasks.OperationFlag;
import com.github.malyszaryczlowiek.cpcdb.tasks.UpdateRemoteDatabase;
import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.*;
import com.github.malyszaryczlowiek.cpcdb.alerts.*;
import com.github.malyszaryczlowiek.cpcdb.managers.initializers.*;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
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
        AskToSaveChangesBeforeQuitController.SaveOrCancelListener,
        MergingRemoteDbController.Mergeable
{
    // main table
    @FXML private TableView<Compound> mainSceneTableView;

    // table's columns    //@FXML private TableColumn<Compound, Integer> idCol;
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
    @FXML private MenuItem menuFileAddCompound;
    @FXML private MenuItem menuFileSearch;
    @FXML private MenuItem menuFileLoadFullTable;
    @FXML private MenuItem menuFileLoadDataFromRemoteServer;
    @FXML private MenuItem menuFileSave;
    @FXML private MenuItem menuFileSettings;
    @FXML private MenuItem menuFileQuit;

    // Edit ->

    @FXML private MenuItem menuEditUndo;
    @FXML private MenuItem menuEditRedo;
    @FXML private MenuItem menuEditSelectedCompound;
    @FXML private MenuItem menuEditDeleteSelectedCompounds;

    // View -> Full Screen
    @FXML private CheckMenuItem menuViewFullScreen;
    @FXML private MenuItem menuViewShowAllColumns;

    // Help -> About CPCDB
    @FXML private MenuItem menuHelpAboutCPCDB;

    // Context menu of table
    @FXML private MenuItem editSelectedCompoundContext;
    @FXML private MenuItem deleteSelectedCompoundsContext;
    @FXML private MenuItem showAllColumnsContext;

    // progress bar
    @FXML private ProgressBar progressBar;
    @FXML private Text currentStatus;

    private Stage primaryStage;
    private boolean unblockingTask3 = false;
    private BufferExecutor bufferExecutor;
    private ColumnManager columnManager;
    private List<Compound> fullListOfCompounds; // to jest lista którą trzeba używać jak chcemy zapisać dane do bazy danych
    private ObservableList<Compound> observableList;
    private LaunchTimer initializationTimer;
    private LockProvider lockProvider;
    private CurrentStatusManager currentStatusManager;
    private ScheduledService<Void> databasePingService = null;

    //public MainStageController(ScheduledService<Void> databasePingService) {this.databasePingService = databasePingService;}


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
            currentStatusManager = CurrentStatusManager.getThisCurrentStatusManager(currentStatus, progressBar);
            currentStatusManager.setInfoStatus("Initializing program");
            lockProvider = LockProvider.getLockProvider();
            fullListOfCompounds = new ArrayList<>();
            observableList = FXCollections.observableArrayList(fullListOfCompounds); // todo TE DWIE LINIJKI MOŻNA chyba przenieść do loadingdatabaseTask  12 linijek niżej
            mainSceneTableView.setItems(observableList);
            bufferExecutor = BufferExecutor.getBufferExecutor( mainSceneTableView, observableList,
                    menuEditUndo, menuEditRedo, menuFileSave);
            columnManager = new ColumnManager( new MenuItem[]{menuViewShowAllColumns, showAllColumnsContext},
                    smilesCol, compoundNumCol, amountCol, unitCol, formCol, tempStabilityCol,
                    argonCol, containerCol, storagePlaceCol, lastModificationCol, additionalInfoCol);
            ColumnInitializer.setUpInitializer(mainSceneTableView, bufferExecutor, observableList);
            ErrorFlagsManager.initializeErrorFlagsManager();
            if ( !SecureProperties.hasProperty("column.width.Smiles") ) columnManager.setAllColumnsWidthToProperties();
            Task<String> loadingDatabaseTask = LoadRemoteDatabase.getTask(fullListOfCompounds, observableList,
                    mainSceneTableView, this, databasePingService, true, OperationFlag.LOADING);
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
            primaryStage.setOnCloseRequest( windowEvent -> {
                windowEvent.consume();
                onMenuFileQuit();
            });
            mainSceneTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            mainSceneTableView.setOnContextMenuRequested( contextMenuEvent -> {
                int count = mainSceneTableView.getSelectionModel().getSelectedItems().size();
                if ( count == 1 ) editSelectedCompoundContext.setDisable(false); // Edit Selected Compound
                else editSelectedCompoundContext.setDisable(true);
                if ( count >= 1 ) deleteSelectedCompoundsContext.setDisable(false);//Delete Selected Compound(s)
                else deleteSelectedCompoundsContext.setDisable(true);
            });
        }
        currentStatusManager.addToProgressValue(0.3);
        initializationTimer.stopTimer("initializing process completed");
    }

    private void settingUpTask1() {
        columnManager.initializeColumns(Field.SMILES, Field.COMPOUNDNUMBER, Field.AMOUNT, Field.UNIT);
        MenuItemInitializer.initialize(menuFileAddCompound, menuFileLoadFullTable, menuFileSave,
                menuFileSearch, menuFileSettings, menuFileQuit, menuEditSelectedCompound,
                menuViewFullScreen, menuHelpAboutCPCDB, menuFileLoadDataFromRemoteServer);
        menuViewFullScreen.setSelected(false);
        synchronized (lockProvider.getLock(LockTypes.INITIALIZATION_END)) {
            if (!unblockingTask3) unblockingTask3 = true;
            else lockProvider.getLock(LockTypes.INITIALIZATION_END).notifyAll();
        }
    }

    private void settingUpTask2() {
        columnManager.initializeColumns(Field.FORM, Field.TEMPSTABILITY, Field.ARGON, Field.CONTAINER);
        synchronized (lockProvider.getLock(LockTypes.INITIALIZATION_END)) {
            if (!unblockingTask3) unblockingTask3 = true;
            else lockProvider.getLock(LockTypes.INITIALIZATION_END).notifyAll();
        }
    }

    private void settingUpTask3() {
        columnManager.initializeColumns(Field.STORAGEPLACE, Field.DATETIMEMODIFICATION, Field.ADDITIONALINFO);
        try {
            synchronized (lockProvider.getLock(LockTypes.INITIALIZATION_END)) {
                lockProvider.getLock(LockTypes.INITIALIZATION_END).wait();
            }
        }
        catch (InterruptedException e) { e.printStackTrace(); }
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
    protected void menuFileAddCompound() {
        WindowFactory.showWindow(WindowsEnum.ADD_COMPOUND_WINDOW, this, null);
    }

    // FILE -> Search
    @FXML
    protected void onFileSearchMenuItemClicked() {
        WindowFactory.showWindow(WindowsEnum.SEARCH_COMPOUND_WINDOW, null, null);
    }

    // FILE -> Save
    /**
     * metoda uruchamiana po kliknięciu save w menu programu
     */
    @FXML
    protected void onMenuFileSaveClicked() { // saving changes
        if ( bufferExecutor.returnCurrentIndex() > 0 ) {
            Task<String> saveTask = UpdateRemoteDatabase.getTask( fullListOfCompounds, observableList,
                    mainSceneTableView, this, databasePingService,true );
            Thread saveThread = new Thread(saveTask);
            saveThread.setDaemon(true);
            saveThread.start();
        }
    }

    @FXML
    protected void onMenuFileSettingsClicked() {
        WindowFactory.showWindow(WindowsEnum.SETTINGS_WINDOW,null, null);
    }

    @FXML
    protected void onMenuFileLoadDataFromRemoteServer() {
        if ( bufferExecutor.returnCurrentIndex() > 0 ) { // merging
            Task<String> saveTask = UpdateRemoteDatabase.getTask( fullListOfCompounds, observableList,
                    mainSceneTableView, this, databasePingService, true );
            Thread saveThread = new Thread(saveTask);
            saveThread.setDaemon(true);
            saveThread.start();
        }
        else { // simple load
            Task<String> loadingWithoutMergingTask = LoadRemoteDatabase.getTask( fullListOfCompounds, observableList,
                    mainSceneTableView, this, databasePingService,true, OperationFlag.LOADING);
            Thread loadingWithoutMergingThread = new Thread(loadingWithoutMergingTask);
            loadingWithoutMergingThread.setDaemon(true);
            loadingWithoutMergingThread.start();
        }
    }

    // FILE -> Quit

    @FXML
    protected void onMenuFileQuit() {
        if ( bufferExecutor.returnCurrentIndex() > 0 )
            WindowFactory.showWindow(WindowsEnum.SAVE_CHANGES_BEFORE_QUIT_WINDOW,this, null);
        else onCloseProgramWithoutChanges();
    }

    // EDIT

    @FXML
    protected void menuEdit() {
        int count = mainSceneTableView.getSelectionModel().getSelectedItems().size();
        if (count == 1) menuEditSelectedCompound.setDisable(false);         // Edit -> Edit Selected Compound
        else menuEditSelectedCompound.setDisable(true);
        if (count >= 1) menuEditDeleteSelectedCompounds.setDisable(false); // Edit -> Delete Selected Compound(s)
        else menuEditDeleteSelectedCompounds.setDisable(true);
    }

    // EDIT -> Undo

    @FXML
    protected void onUndoClicked() { bufferExecutor.undo(); } // robię undo

    @FXML
    protected void onRedoClicked() { bufferExecutor.redo(); } // robię redo

    @FXML
    protected void onMenuEditSelectAll() { mainSceneTableView.getSelectionModel().selectAll(); }

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
    protected void onMenuShowAllColumns() { columnManager.onShowHideAllColumns(); }


    @FXML
    protected void showEditCompoundStage() {
        Compound selectedItems = mainSceneTableView.getSelectionModel().getSelectedItems().get(0);
        WindowFactory.showWindow(WindowsEnum.EDIT_COMPOUND_WINDOW, this, selectedItems);
    }


    @Override
    public void notifyAboutAddedCompound(Compound compound) {
        observableList.add(compound);
        Integer index = observableList.indexOf(compound);
        Map<Integer, Compound> toInsert = new TreeMap<>();
        toInsert.put(index, compound);
        bufferExecutor.addChange(ActionType.INSERT, toInsert,null,null); //  make insert to buffer
    }


    /**
     * Metoda wywołana z interfejsu
     * SearchCompoundStageController.OnChosenSearchingCriteriaListener
     * która ma za zadanie odfiltrowanie compoundów spełniających żadane kryteria
     * a następnie umieszczenie znalezionych związków w tabeli
     */
    @Override
    public void searchingCriteriaChosen(String smiles, String smilesAccuracy, String compoundNumber,
                                        String compoundNumberAccuracy, String form, String container,
                                        String storagePlace, String beforeAfter, LocalDate selectedLocalDate,
                                        String argon, String temperature, String additionalInfo)
    {
        SearchEngine searchEngine = new SearchEngine(fullListOfCompounds, smiles, smilesAccuracy, // todo to można byłoby zrobić w innym watku !!!
                compoundNumber, compoundNumberAccuracy, form, container, storagePlace, beforeAfter,
                selectedLocalDate, argon, temperature, additionalInfo);
        if (!searchEngine.returnListOfFoundCompounds().isEmpty()) { // display found compounds
            observableList.clear();
            observableList.setAll( searchEngine.returnListOfFoundCompounds() );
            mainSceneTableView.refresh();
        }
        else ShortAlertWindowFactory.showWindow(ErrorType.NOT_FOUND_COMPOUND);
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
        bufferExecutor.addChange(ActionType.REMOVE, mapOfCompounds, null, null);
        fullListOfCompounds.clear();
        fullListOfCompounds.addAll(observableList.sorted());
        event.consume();
    }

    /*
     * ###############################################
     * METHODS TO CLOSE PROGRAM
     * ###############################################
     */

    @Override
    public void onSaveChangesAndCloseProgram() {
        LaunchTimer closeTimer = new LaunchTimer();
        Task<String> saveTask = UpdateRemoteDatabase.getTask( fullListOfCompounds, observableList,
                mainSceneTableView, this, databasePingService,false );
        Thread saveThread = new Thread(saveTask);
        saveThread.start();
        Task<Void> task2 = new Task<>()
        {
            @Override
            protected Void call() {
                columnManager.setAllColumnsWidthToProperties();
                SecureProperties.saveProperties();
                primaryStage.close();
                return null;
            }
        };
        Thread thread2 = new Thread(task2);
        thread2.start();
        try {
            thread2.join();
            saveThread.join();
        }
        catch (InterruptedException e) { e.printStackTrace(); }
        closeTimer.stopTimer("closing time when must save data to DB");
        Platform.exit();
    }

    @Override
    public void onCloseProgramWithoutChanges() {
        LaunchTimer closeTimer = new LaunchTimer();
        columnManager.setAllColumnsWidthToProperties();
        SecureProperties.saveProperties();
        closeTimer.stopTimer("closing time when NOT saving changes only properties");
        Platform.exit();
    }

    @FXML
    protected void onShowHideColumn() {
        WindowFactory.showWindow(WindowsEnum.COLUMN_MANAGER_WINDOW, null, null);
    }

    @FXML
    private void onCurrentStatusTextClicked() { currentStatusManager.onCurrentStatusTextClicked(); }

    /**
     * Method implemented from interface MergingRemoteDbController.Mergeable
     */
    @Override
    public void mergeWithRemote() { // tutaj będzie najpierw save, potem wczytanie danych ze zdalnej bazy danych, wyświetlenie i zapisanie w local
        Task<String> mergeRemote = UpdateRemoteDatabase.getTask(fullListOfCompounds, observableList,
                mainSceneTableView, this, databasePingService, true);
        Thread mergingThread = new Thread(mergeRemote);
        mergingThread.setDaemon(true);
        mergingThread.start();
    }

    /**
     * Method implemented from interface MergingRemoteDbController.Mergeable
     */
    @Override
    public void loadFromRemoteWithoutMerging() {
        Task<String> loadingWithoutMergingTask = LoadRemoteDatabase.getTask( fullListOfCompounds, observableList,
                mainSceneTableView, this, databasePingService, true, OperationFlag.LOADING);
        Thread loadingWithoutMergingThread = new Thread(loadingWithoutMergingTask);
        loadingWithoutMergingThread.setDaemon(true);
        loadingWithoutMergingThread.start();
    }
}





