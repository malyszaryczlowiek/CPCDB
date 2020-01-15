package com.github.malyszaryczlowiek.cpcdb.controllers;

import com.github.malyszaryczlowiek.cpcdb.windows.alertWindows.*;
import com.github.malyszaryczlowiek.cpcdb.alerts.*;
import com.github.malyszaryczlowiek.cpcdb.buffer.ActionType;
import com.github.malyszaryczlowiek.cpcdb.buffer.ChangesDetector;
import com.github.malyszaryczlowiek.cpcdb.initializers.*;
import com.github.malyszaryczlowiek.cpcdb.managers.ColumnManager;
import com.github.malyszaryczlowiek.cpcdb.compound.*;
import com.github.malyszaryczlowiek.cpcdb.db.ConnectionManager;
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
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


public class MainStageController implements Initializable,
        AddCompoundStageController.CompoundAddedListener, // Added live updating of TableView using
        SearchCompoundStageController.SearchingCriteriaChosenListener,
        EditCompoundStageController.EditChangesStageListener,
        AskToSaveChangesBeforeQuitController.SaveOrCancelListener
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
    @FXML private MenuItem menuFileLoadFullTable;
    @FXML private MenuItem menuFileSave;
    @FXML private MenuItem menuFileSearch;
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
    private ChangesDetector changesDetector;
    private ColumnManager columnManager;
    private List<Compound> fullListOfCompounds;
    private ObservableList<Compound> observableList;
    private LaunchTimer initializationTimer;
    private LaunchTimer demonTimer;
    private ScheduledService<Void> databasePingService;

    private LockProvider lockProvider;
    private CurrentStatusManager currentStatusManager;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializationTimer = new LaunchTimer();

        if ( checkIfAskForDBProperties() )
            WindowFactory.showWindow(WindowsEnum.INITIALIZING_DB_PROPERTIES_WINDOW, null, null);
        else {
            LaunchTimer keyAndPropertiesLoading = new LaunchTimer();
            SecureProperties.loadProperties();
            keyAndPropertiesLoading.stopTimer("key and properties loading time: ");
            CloseProgramNotifier.setToNotCloseProgram();
        }

        if ( ! CloseProgramNotifier.getIfCloseUninitializedProgram() ) {
            progressValue = new SimpleDoubleProperty(0.0);
            currentStatusManager = new CurrentStatusManager(currentStatus);
            currentStatusManager.setCurrentStatus("Initializing program");
            changesDetector = new ChangesDetector();
            lockProvider = new LockProvider();
            fullListOfCompounds = new ArrayList<>();
            observableList = FXCollections.observableArrayList(fullListOfCompounds);
            mainSceneTableView.setItems(observableList);
            columnManager = new ColumnManager(
                    new MenuItem[]{menuViewShowAllColumns, showAllColumnsContext},
                    smilesCol, compoundNumCol, amountCol, unitCol, formCol, tempStabilityCol,
                    argonCol, containerCol, storagePlaceCol, lastModificationCol, additionalInfoCol);
            ColumnInitializer.setUpInitializer(mainSceneTableView, changesDetector);
            ErrorFlagsManager.initializeErrorFlagsManager();
            if ( !SecureProperties.hasProperty("column.width.Smiles") )
                setWidthOfColumns();
            demonTimer = new LaunchTimer();
            Task<String> loadingDatabaseTask = new Task<>()
            {
                @Override
                protected String call()  {
                        try (Connection connection = ConnectionManager.connectToDb()) {
                            // not working on any copy
                            if ( ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR)
                                    && ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR) )
                                updateValue("cannotConnectToAllDB");
                                // not working on any copy
                            else if ( ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR)
                                    && ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR) )
                                updateValue("cannotConnectToRemoteDatabaseAndIncorrectLocalUsernameOrPassphrase");
                                // working on local copy
                            else if ( ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR)
                                    && !ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR)
                                    && !ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR)
                                    && connection != null ) {
                                updateValue("cannotConnectToRemoteDB");
                                stopThisThread(50);
                                loadTable(connection);
                            }
                            // not working on any copy
                            else if ( ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR)
                                    && ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR) )
                                updateValue("incorrectRemotePassphraseAndCannotConnectToLocalDatabase");
                                // not working on any copy
                            else if ( ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR)
                                    && ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR) )
                                updateValue("incorrectRemoteAndLocalPassphrase");
                                // working on local copy
                            else if ( ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR)
                                    && !ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR)
                                    && !ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR)
                                    && connection != null) {
                                updateValue("incorrectRemotePassphrase");
                                stopThisThread(50);
                                loadTable(connection);
                            } // all working ok
                            else if (connection != null //)
                                    && ErrorFlagsManager.getMapOfErrors().values().stream().noneMatch(value -> value) )
                                loadTable(connection);
                            else
                                updateMessage("UnknownErrorOccurred");
                        }
                        catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                private void stopThisThread(int milliseconds) {
                    try {
                        synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                            lockProvider.getLock(LockTypes.PROGRESS_VALUE).wait(milliseconds);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                /*
                 * ###############################################
                 * METHODS TO LOAD CONTENT OF TABLE
                 * ###############################################
                 */
                private void loadTable(Connection connection)  {
                    updateMessage("Connecting to Database");
                    stopThisThread(1);
                    final String numberOfRowsQuery = "SELECT COUNT(*) FROM compounds";
                    int size = 0;
                    try {
                        PreparedStatement loadDBStatement = connection.prepareStatement(numberOfRowsQuery);
                        ResultSet resultSet = loadDBStatement.executeQuery();
                        while (resultSet.next())
                            size = resultSet.getInt(1); // getting number of rows
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    final String loadDBSQLQuery = "SELECT * FROM compounds";
                    try {
                        PreparedStatement loadDBStatement = connection.prepareStatement(loadDBSQLQuery);
                        synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                            progressValue.setValue( progressValue.get() + 0.1);
                        }
                        updateMessage("Downloading data from server");
                        stopThisThread(1);
                        ResultSet resultSet = loadDBStatement.executeQuery();
                        updateMessage("Loading downloaded data");
                        stopThisThread(1);
                        int index = 0;
                        while(resultSet.next()) {
                            int id = resultSet.getInt(1);
                            String smiles = resultSet.getString(2);
                            String compoundName = resultSet.getString(3);
                            float amount = resultSet.getFloat(4);
                            String unit = resultSet.getString(5);
                            String form = resultSet.getString(6);
                            String tempStability = resultSet.getString(7);
                            boolean argon = resultSet.getBoolean(8);
                            String container = resultSet.getString(9);
                            String storagePlace = resultSet.getString(10);
                            LocalDateTime dateTimeModification = resultSet.getTimestamp(11).toLocalDateTime();
                            String additionalInformation = resultSet.getString(12);
                            Compound compound = new Compound(smiles, compoundName, amount, Unit.stringToEnum(unit),
                                    form, TempStability.stringToEnum(tempStability), argon, container,
                                    storagePlace, dateTimeModification, additionalInformation);
                            compound.setId(id);
                            compound.setSavedInDatabase(true);
                            fullListOfCompounds.add(compound);
                            try {
                                Thread.sleep(0);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            double loadedPercentage = Math.round( (double) ++index / ((double) size) * 100);
                            synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                                progressValue.setValue( progressValue.get() + 0.6 * ( 1.0 / ((double) size)));
                            }
                            if (index % 100 == 0) {
                                updateMessage("Loaded " + loadedPercentage + "%");
                                stopThisThread(1);
                            }
                        }
                        updateMessage("Refreshing table");
                        stopThisThread(1);
                        observableList.setAll(fullListOfCompounds);
                        mainSceneTableView.setItems(observableList);
                    }
                    catch (SQLException e) {
                        e.printStackTrace();
                    }
                    synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                        progressValue.setValue(0.0);
                    }
                    boolean showWarning = ( ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR)
                            && !ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR)
                            && !ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR) )
                            ||
                            ( ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_REMOTE_DB_ERROR)
                                    && !ErrorFlagsManager.getError(ErrorFlags.INCORRECT_USERNAME_OR_PASSPHRASE_TO_LOCAL_DB_ERROR)
                                    && !ErrorFlagsManager.getError(ErrorFlags.CONNECTION_TO_LOCAL_DB_ERROR) );
                    if ( showWarning )
                        updateMessage("showWarning");
                    else
                        updateMessage("Data loaded, table refreshed");
                    stopThisThread(1);
                    demonTimer.stopTimer("stopping demon timer");
                }
            };

            loadingDatabaseTask.messageProperty().addListener(
                    (observableValue, oldString, newString) -> manageConnectingToDbCommunicates(newString) );

            loadingDatabaseTask.valueProperty().addListener(
                    (observable, oldValue, newValue) -> manageConnectingToDbCommunicates(newValue) );


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

    private boolean checkIfAskForDBProperties() {
         return !( new File("propertiesFile").exists() );
    }


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
                        if ( count == 1 ) // Edit Selected Compound
                            editSelectedCompoundContext.setDisable(false);
                        else
                            editSelectedCompoundContext.setDisable(true);
                        if ( count >= 1 ) //Delete Selected Compound(s)
                            deleteSelectedCompoundsContext.setDisable(false);
                        else
                            deleteSelectedCompoundsContext.setDisable(true);
                    });
        }
        synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
            progressValue.setValue( progressValue.get() + 0.3);
        }
        initializationTimer.stopTimer("initializing process completed");
    }

    /*
    * ###############################################
    * FUNCTIONS FROM MENUS ITEMS
    * ###############################################
    */

    // FILE

    /**
     * Method called when user click on FILE menu
     */
    @FXML
    protected void menuFile() {
        if ( changesDetector.returnCurrentIndex() > 0) {
            menuFileSave.setDisable(false);
            System.out.println("menu save is enabled");
        }
        else {
            menuFileSave.setDisable(true);
            System.out.println("menu save is disabled");
        }
    }


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
        if ( changesDetector.returnCurrentIndex() > 0 )
            changesDetector.saveChangesToDatabase();
    }

    @FXML
    protected void onMenuFilePreferencesClicked(ActionEvent event) {
        event.consume();
        WindowFactory.showWindow(WindowsEnum.SETTINGS_WINDOW,null, null);
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

        // Edit -> Undo
        if ( changesDetector.returnCurrentIndex() > 0 )
            menuEditUndo.setDisable(false);
        else
            menuEditUndo.setDisable(true);


        // Edit -> Redo
        if ( changesDetector.isBufferNotOnLastPosition() )
            menuEditRedo.setDisable(false);
        else
            menuEditRedo.setDisable(true);


        // Edit -> Edit Selected Compound
        if ( count == 1 )
            menuEditSelectedCompound.setDisable(false);
        else
            menuEditSelectedCompound.setDisable(true);


        // Edit -> Delete Selected Compound(s)
        if ( count >= 1 )
            menuEditDeleteSelectedCompounds.setDisable(false);
        else
            menuEditDeleteSelectedCompounds.setDisable(true);
    }

    // EDIT -> Undo

    @FXML
    protected void onUndoClicked() {
        if ( changesDetector.returnCurrentIndex() > 0 ) {
            try {
                Map<Integer, Compound> mapOfCompoundsToChangeInTableView = changesDetector.undo();
                ActionType actionType = changesDetector.getActionTypeOfCurrentOperation();
                if ( mapOfCompoundsToChangeInTableView != null )
                    executeUndoRedo( mapOfCompoundsToChangeInTableView, actionType );
                mainSceneTableView.refresh();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void onRedoClicked() {
        if ( changesDetector.isBufferNotOnLastPosition() ) {
            try {
                Map<Integer, Compound> mapOfCompoundsToChangeInTableView = changesDetector.redo();
                ActionType actionType = changesDetector.getActionTypeOfCurrentOperation();
                if ( mapOfCompoundsToChangeInTableView != null )
                    executeUndoRedo( mapOfCompoundsToChangeInTableView, actionType );
                mainSceneTableView.refresh();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void onMenuEditSelectAll() {
        mainSceneTableView.getSelectionModel().selectAll();
        /*
        ObservableList<Integer> lastSelectedCompounds = mainSceneTableView.getSelectionModel().getSelectedIndices();
        int size = lastSelectedCompounds.size();
        int[] selectedIndexes = new int[size];
        int i = 0;
        for (int index: lastSelectedCompounds) {
            System.out.println("index: " + index);
            selectedIndexes[i++] = index;
        }
         */
    }

    private void executeUndoRedo(Map<Integer, Compound> mapOfCompoundsToChangeInTableView, ActionType actionType) {
        if ( actionType.equals( ActionType.REMOVE ) ) {
            if ( mapOfCompoundsToChangeInTableView
                    .values()
                    .stream()
                    .allMatch( Compound::isToDelete )
            )
                observableList.removeAll( mapOfCompoundsToChangeInTableView.values() );
            else
                mapOfCompoundsToChangeInTableView.forEach(
                        (index, compound) -> observableList.add(index, compound)
                );

        }
        if ( actionType.equals( ActionType.INSERT ) ) {
            if ( mapOfCompoundsToChangeInTableView
                    .values()
                    .stream()
                    .allMatch( Compound::isToDelete )
            )
                observableList.removeAll( mapOfCompoundsToChangeInTableView.values() );
            else
                mapOfCompoundsToChangeInTableView.forEach(
                        (index, compound) -> observableList.add(index, compound)
                );
        }
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
        try {
            changesDetector.makeInsert(toInsert);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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
        boolean empty = searchEngine.returnListOfFoundCompounds().isEmpty();        // display found compounds
        if (!empty) {
            observableList.clear();
            observableList.setAll(searchEngine.returnListOfFoundCompounds());
            mainSceneTableView.refresh();
        }
        else
            ShortAlertWindowFactory.showErrorWindow(ErrorType.NOT_FOUND_COMPOUND);
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
        selectedItems.forEach(
                compound ->
                        mapOfCompounds.put( observableList.indexOf( compound ), compound )
        );
        changesDetector.makeDelete(mapOfCompounds);
        observableList.removeAll(selectedItems.sorted());
        mainSceneTableView.refresh();
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
        if ( changesDetector.returnCurrentIndex() > 0 )
            WindowFactory.showWindow(WindowsEnum.SAVE_CHANGES_BEFORE_QUIT_WINDOW,this, null);
        else
            onCloseProgramWithoutChanges();
    }

    @Override
    public void reloadTableAfterCompoundEdition() {
        mainSceneTableView.refresh();
    }

    @Override
    public void reloadTableAfterCompoundDeleting(Compound compound) {
        Map<Integer, Compound> compoundToDelete = new TreeMap<>();
        compoundToDelete.put( observableList.indexOf(compound), compound );
        changesDetector.makeDelete(compoundToDelete);
        observableList.remove(compound);
        mainSceneTableView.refresh();
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
                changesDetector.saveChangesToDatabase();
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
        catch (InterruptedException e) {
            e.printStackTrace();
        }
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


    private void settingUpTask1() {
        new SmilesColumnInitializer(smilesCol).initialize();
        new CompoundNumberInitializer(compoundNumCol).initialize();
        new AmountColumnInitializer(amountCol).initialize();
        new UnitColumnInitializer(unitCol).initialize();
        MenuItemInitializer.initialize(menuFileAddCompound, menuFileLoadFullTable, menuFileSave,
                menuFileSearch,menuFilePreferences, menuFileQuit, menuEditSelectedCompound,
                menuViewFullScreen, menuHelpAboutCPCDB);
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
        if (text.equals("Connection to Remote Database Established"))
        WindowFactory.showWindow(WindowsEnum.MERGING_REMOTE_DB_WINDOW,null,null);
        currentStatusManager.resetFont();
    }

    private void startService() {
        databasePingService = new ScheduledService<>()
        {
            @Override
            protected Task<Void> createTask() {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        try (Connection connection = ConnectionManager.reconnectToRemoteDb()) {
                            if (connection != null)
                                updateMessage("connectionEstablished");
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
                task.messageProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.equals("connectionEstablished")) {
                        currentStatusManager.setGreenFont();
                        currentStatusManager.setCurrentStatus("Connection to Remote Database Established");
                        databasePingService.cancel();
                        ErrorFlagsManager.setErrorFlagTo(ErrorFlags.CONNECTION_TO_REMOTE_DB_ERROR, false);
                    }
                });
                return task;
            }
        };
        databasePingService.setPeriod(Duration.seconds(3));
        databasePingService.start();
    }

    private void manageConnectingToDbCommunicates(String newString) {
        switch (newString) {
            case "cannotConnectToAllDB":
                ShortAlertWindowFactory.showErrorWindow(ErrorType.CANNOT_CONNECT_TO_ALL_DB);
                startService();
                synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                    progressBar.setProgress(0.0);
                    currentStatusManager.setErrorMessage("Error (click here for more info)");
                    lockProvider.getLock(LockTypes.PROGRESS_VALUE).notifyAll();
                }
                break;
            case "cannotConnectToRemoteDatabaseAndIncorrectLocalUsernameOrPassphrase":
                ShortAlertWindowFactory.showErrorWindow(ErrorType.CANNOT_CONNECT_TO_REMOTE_BD_AND_INCORRECT_LOCAL_PASSPHRASE);
                startService();
                synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                    progressBar.setProgress(0.0);
                    currentStatusManager.setErrorMessage("Error (click here for more info)");
                    lockProvider.getLock(LockTypes.PROGRESS_VALUE).notifyAll();
                }
                break;
            case "cannotConnectToRemoteDB":
                ShortAlertWindowFactory.showErrorWindow(ErrorType.CANNOT_CONNECT_TO_REMOTE_DB);
                startService();
                synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                    lockProvider.getLock(LockTypes.PROGRESS_VALUE).notifyAll();
                }
                break;
            case "incorrectRemotePassphraseAndCannotConnectToLocalDatabase":
                ShortAlertWindowFactory.showErrorWindow(ErrorType.INCORRECT_REMOTE_PASSPHRASE_AND_CANNOT_CONNECT_TO_LOCAL_DB);
                synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                    progressBar.setProgress(0.0);
                    currentStatusManager.setErrorMessage("Error (click here for more info)");
                    lockProvider.getLock(LockTypes.PROGRESS_VALUE).notifyAll();
                }
                break;
            case "incorrectRemoteAndLocalPassphrase":
                ShortAlertWindowFactory.showErrorWindow(ErrorType.INCORRECT_REMOTE_AND_LOCAL_PASSPHRASE);
                synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                    progressBar.setProgress(0.0);
                    currentStatusManager.setErrorMessage("Error (click here for more info)");
                    lockProvider.getLock(LockTypes.PROGRESS_VALUE).notifyAll();
                }
                break;
            case "incorrectRemotePassphrase":
                ShortAlertWindowFactory.showErrorWindow(ErrorType.INCORRECT_REMOTE_PASSPHRASE);
                synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                    currentStatusManager.setCurrentStatus("Warning (click here for more info)");
                    lockProvider.getLock(LockTypes.PROGRESS_VALUE).notifyAll();
                }
                break;
            case "showWarning":
                synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                    progressBar.setProgress(0.0);
                    currentStatusManager.setWarningMessage("Data loaded. Warning (click here for info)");
                    lockProvider.getLock(LockTypes.PROGRESS_VALUE).notifyAll();
                }
                break;
            case "UnknownErrorOccurred":
                ShortAlertWindowFactory.showErrorWindow(ErrorType.UNKNOWN_ERROR_OCCURRED);
                synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                    progressBar.setProgress(0.0);
                    currentStatusManager.setErrorMessage("Unknown Error Occurred");
                    lockProvider.getLock(LockTypes.PROGRESS_VALUE).notifyAll();
                }
                break;
            default:  // correct data loading
                progressBar.setProgress(progressValue.doubleValue());
                currentStatusManager.setCurrentStatus(newString);
                synchronized (lockProvider.getLock(LockTypes.PROGRESS_VALUE)) {
                    lockProvider.getLock(LockTypes.PROGRESS_VALUE).notifyAll();
                }
                break;
        }
    }
}













