package com.example.autoclicker.ui;

import com.example.autoclicker.automation.*;
import com.example.autoclicker.config.AutomationConfig;
import com.example.autoclicker.config.JsonConfigRepository;
import com.example.autoclicker.image.ImageDetector;
import com.example.autoclicker.image.MatchResult;
import com.example.autoclicker.image.ScreenshotService;
import com.example.autoclicker.screen.ScreenInfo;
import com.example.autoclicker.screen.ScreenManager;
import com.example.autoclicker.util.I18n;
import com.example.autoclicker.util.LoggerUtil;
import com.example.autoclicker.window.WindowInfo;
import com.example.autoclicker.window.WindowManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;

import java.awt.Rectangle;
import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main UI Controller for FCOAutoClick with i18n support, Toolbar controls, and dynamic CHECK_IMAGE branching.
 * Developed by TunEngDev2003.
 */
public class MainController {

    // Top Header & Brand
    @FXML private ImageView appIconView;
    @FXML private Label appTitleLabel;
    @FXML private Label authorBadgeLabel;
    @FXML private Label appSubtitleLabel;
    @FXML private Label engineStatusLabel;
    @FXML private Label engineStatusBadge;
    @FXML private Label loopCounterLabel;

    // Top Toolbar Controls
    @FXML private MenuButton optionsMenuButton;
    @FXML private MenuItem openProfileMenuItem;
    @FXML private MenuItem saveProfileMenuItem;
    @FXML private Menu languageSubMenu;
    @FXML private RadioMenuItem langEnMenuItem;
    @FXML private RadioMenuItem langViMenuItem;
    @FXML private Label toolbarWindowLabel;
    @FXML private ComboBox<WindowInfo> windowComboBox;
    @FXML private Button refreshWindowsBtn;
    @FXML private Label windowStatusLabel;
    @FXML private Label toolbarScreenLabel;
    @FXML private ComboBox<ScreenInfo> screenComboBox;
    @FXML private Button startButton;
    @FXML private Button stopButton;

    // Repeat Count Input
    @FXML private Label repeatCountLabel;
    @FXML private TextField repeatCountField;

    // Execution Mode & Marker Checkboxes
    @FXML private CheckBox backgroundClickCheckBox;
    @FXML private CheckBox showMarkerCheckBox;

    // Main Actions Workspace (3 Tabs)
    @FXML private TabPane branchTabPane;

    @FXML private Tab initialTab;
    @FXML private Label initialTabDesc;
    @FXML private ListView<AutomationAction> initialListView;
    @FXML private Button addInitialBtn;
    @FXML private Button addCheckImageBtn;
    @FXML private Button pickCoordInitialBtn;
    @FXML private Button editInitialBtn;
    @FXML private Button delInitialBtn;
    @FXML private Button upInitialBtn;
    @FXML private Button downInitialBtn;

    @FXML private Tab branchATab;
    @FXML private Label branchATabDesc;
    @FXML private ListView<AutomationAction> branchAListView;
    @FXML private Button addBranchABtn;
    @FXML private Button pickCoordBranchABtn;
    @FXML private Button editBranchABtn;
    @FXML private Button delBranchABtn;
    @FXML private Button upBranchABtn;
    @FXML private Button downBranchABtn;

    @FXML private Tab branchBTab;
    @FXML private Label branchBTabDesc;
    @FXML private ListView<AutomationAction> branchBListView;
    @FXML private Button addBranchBBtn;
    @FXML private Button pickCoordBranchBBtn;
    @FXML private Button editBranchBBtn;
    @FXML private Button delBranchBBtn;
    @FXML private Button upBranchBBtn;
    @FXML private Button downBranchBBtn;

    // Log Console & Footer
    @FXML private Label logConsoleTitle;
    @FXML private Button clearLogBtn;
    @FXML private TextArea logArea;
    @FXML private Label footerCopyrightLabel;

    // Managers & Services
    private ScreenManager screenManager;
    private WindowManager windowManager;
    private ScreenshotService screenshotService;
    private ImageDetector imageDetector;
    private ActionExecutor actionExecutor;
    private AutomationEngine automationEngine;
    private JsonConfigRepository configRepository;

    private final ObservableList<AutomationAction> initialActions = FXCollections.observableArrayList();
    private final ObservableList<AutomationAction> branchAActions = FXCollections.observableArrayList();
    private final ObservableList<AutomationAction> branchBActions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        screenManager = new ScreenManager();
        windowManager = new WindowManager();
        screenshotService = new ScreenshotService();
        imageDetector = new ImageDetector();
        actionExecutor = new ActionExecutor();
        automationEngine = new AutomationEngine(screenshotService, imageDetector, actionExecutor, screenManager, windowManager);
        configRepository = new JsonConfigRepository();

        // Connect ListView data
        initialListView.setItems(initialActions);
        branchAListView.setItems(branchAActions);
        branchBListView.setItems(branchBActions);

        // Custom Cell Factory for beautiful action items
        initialListView.setCellFactory(lv -> new ActionListCell());
        branchAListView.setCellFactory(lv -> new ActionListCell());
        branchBListView.setCellFactory(lv -> new ActionListCell());

        // Double-click to Edit Action
        initialListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                handleEditActionInitial();
            }
        });
        branchAListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                handleEditActionBranchA();
            }
        });
        branchBListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                handleEditActionBranchB();
            }
        });

        // Setup On-screen Target Marker on Action Selection
        setupSelectionMarker(initialListView);
        setupSelectionMarker(branchAListView);
        setupSelectionMarker(branchBListView);

        branchTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            ActionTargetMarkerOverlay.hide();
        });

        showMarkerCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                ActionTargetMarkerOverlay.hide();
            }
        });

        // Language Toggle Group setup
        ToggleGroup langGroup = new ToggleGroup();
        langEnMenuItem.setToggleGroup(langGroup);
        langViMenuItem.setToggleGroup(langGroup);
        if (I18n.LOCALE_VI.equals(I18n.getLocale())) {
            langViMenuItem.setSelected(true);
        } else {
            langEnMenuItem.setSelected(true);
        }

        // App Icon
        try (InputStream iconStream = getClass().getResourceAsStream("/icons/FCOAutoClick_logo.png")) {
            if (iconStream != null) {
                appIconView.setImage(new Image(iconStream));
            }
        } catch (Exception ignored) {
        }

        // Screens ComboBox setup
        populateScreens();

        // Windows ComboBox setup
        populateWindows();
        windowComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateWindowStatus(newVal));

        // Repeat Count filter
        repeatCountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                repeatCountField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        // Engine State Observer
        automationEngine.addStateListener(state -> Platform.runLater(() -> updateEngineStateUI(state)));

        // Loop Counter Observer
        automationEngine.addLoopListener((currentLoop, maxLoops) -> Platform.runLater(() -> {
            if (maxLoops > 0) {
                loopCounterLabel.setText(MessageFormat.format(I18n.get("lbl.loop_status_limit"), currentLoop, maxLoops));
            } else {
                loopCounterLabel.setText(MessageFormat.format(I18n.get("lbl.loop_status_inf"), currentLoop));
            }
        }));

        // Match Observer (Logs match results)
        automationEngine.addMatchListener((result, state) -> Platform.runLater(() -> {
            LoggerUtil.info(String.format("Match confidence = %.2f -> %s", result.getConfidence(),
                    result.isMatched() ? "MATCH (Branch A)" : "NOT MATCH (Branch B)"));
        }));

        // Log Listener
        LoggerUtil.addLogListener(msg -> Platform.runLater(() -> {
            if (logArea != null) {
                logArea.appendText(msg + "\n");
            }
        }));

        applyI18n();
        LoggerUtil.info("FCOAutoClick v1.0.0 [TunEngDev2003] initialized. Ready.");
    }

    @FXML
    private void handleSwitchLangEn() {
        I18n.setLocale(I18n.LOCALE_EN);
        langEnMenuItem.setSelected(true);
        applyI18n();
    }

    @FXML
    private void handleSwitchLangVi() {
        I18n.setLocale(I18n.LOCALE_VI);
        langViMenuItem.setSelected(true);
        applyI18n();
    }

    /**
     * Applies internationalization translations to all UI elements dynamically.
     */
    private void applyI18n() {
        appTitleLabel.setText(I18n.get("app.title"));
        appSubtitleLabel.setText(I18n.get("app.subtitle"));
        authorBadgeLabel.setText(I18n.get("app.author"));
        optionsMenuButton.setText(I18n.get("menu.options"));
        openProfileMenuItem.setText(I18n.get("menu.open_profile"));
        saveProfileMenuItem.setText(I18n.get("menu.save_profile"));
        languageSubMenu.setText(I18n.get("menu.language"));

        toolbarWindowLabel.setText(I18n.get("toolbar.window"));
        toolbarScreenLabel.setText(I18n.get("toolbar.screen"));
        refreshWindowsBtn.setText(I18n.get("btn.refresh_windows"));
        windowComboBox.setPromptText(I18n.get("prompt.select_window"));

        // Tab 1
        initialTab.setText(I18n.get("tab.initial_actions"));
        initialTabDesc.setText(I18n.get("tab.initial_desc"));
        addInitialBtn.setText(I18n.get("btn.add_action"));
        addCheckImageBtn.setText(I18n.get("btn.add_check_image"));
        pickCoordInitialBtn.setText(I18n.get("btn.pick_coord"));
        editInitialBtn.setText(I18n.get("btn.edit"));
        delInitialBtn.setText(I18n.get("btn.delete"));
        upInitialBtn.setText(I18n.get("btn.move_up"));
        downInitialBtn.setText(I18n.get("btn.move_down"));

        // Tab 2
        branchATab.setText(I18n.get("tab.branch_a"));
        branchATabDesc.setText(I18n.get("tab.branch_a_desc"));
        addBranchABtn.setText(I18n.get("btn.add_action"));
        pickCoordBranchABtn.setText(I18n.get("btn.pick_coord"));
        editBranchABtn.setText(I18n.get("btn.edit"));
        delBranchABtn.setText(I18n.get("btn.delete"));
        upBranchABtn.setText(I18n.get("btn.move_up"));
        downBranchABtn.setText(I18n.get("btn.move_down"));

        // Tab 3
        branchBTab.setText(I18n.get("tab.branch_b"));
        branchBTabDesc.setText(I18n.get("tab.branch_b_desc"));
        addBranchBBtn.setText(I18n.get("btn.add_action"));
        pickCoordBranchBBtn.setText(I18n.get("btn.pick_coord"));
        editBranchBBtn.setText(I18n.get("btn.edit"));
        delBranchBBtn.setText(I18n.get("btn.delete"));
        upBranchBBtn.setText(I18n.get("btn.move_up"));
        downBranchBBtn.setText(I18n.get("btn.move_down"));

        engineStatusLabel.setText(I18n.get("lbl.engine_status"));
        startButton.setText(I18n.get("btn.start"));
        stopButton.setText(I18n.get("btn.stop"));
        repeatCountLabel.setText(I18n.get("lbl.repeat_count"));
        repeatCountField.setPromptText(I18n.get("prompt.repeat_count"));
        backgroundClickCheckBox.setText(I18n.get("chk.background_click"));
        showMarkerCheckBox.setText(I18n.get("chk.show_marker"));

        logConsoleTitle.setText(I18n.get("lbl.log_console"));
        clearLogBtn.setText(I18n.get("btn.clear_log"));
        footerCopyrightLabel.setText(I18n.get("app.title") + " v1.0.0 \u2022 " + I18n.get("app.copyright"));

        updateWindowStatus(windowComboBox.getValue());
    }

    private void setupSelectionMarker(ListView<AutomationAction> listView) {
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldAction, newAction) -> {
            if (showMarkerCheckBox != null && !showMarkerCheckBox.isSelected()) {
                ActionTargetMarkerOverlay.hide();
                return;
            }
            if (newAction == null || newAction.getType() == AutomationActionType.WAIT) {
                ActionTargetMarkerOverlay.hide();
                return;
            }
            Rectangle windowBounds = getActiveWindowBounds();
            int screenX = newAction.getX();
            int screenY = newAction.getY();
            if (newAction.getCoordinateMode() == CoordinateMode.WINDOW_RELATIVE && windowBounds != null) {
                screenX = windowBounds.x + newAction.getX();
                screenY = windowBounds.y + newAction.getY();
            }
            String info = String.format("%s (%d, %d)", newAction.getType(), newAction.getX(), newAction.getY());
            ActionTargetMarkerOverlay.show(screenX, screenY, info);
        });
    }

    private void populateScreens() {
        screenManager.refreshScreens();
        screenComboBox.setItems(FXCollections.observableArrayList(screenManager.getScreens()));
        if (!screenManager.getScreens().isEmpty()) {
            screenComboBox.getSelectionModel().select(0);
        }
    }

    private void populateWindows() {
        List<WindowInfo> windows = windowManager.getOpenWindows();
        windowComboBox.setItems(FXCollections.observableArrayList(windows));

        WindowInfo fcoWindow = null;
        for (WindowInfo w : windows) {
            if (w.getTitle().toUpperCase().contains("FCO")) {
                fcoWindow = w;
                break;
            }
        }
        if (fcoWindow != null) {
            windowComboBox.getSelectionModel().select(fcoWindow);
        } else if (!windows.isEmpty() && windowComboBox.getValue() == null) {
            windowComboBox.getSelectionModel().select(0);
        }
        updateWindowStatus(windowComboBox.getValue());
    }

    private void updateWindowStatus(WindowInfo window) {
        if (window == null) {
            windowStatusLabel.setText(I18n.get("lbl.window_not_found"));
            windowStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f43f5e; -fx-font-weight: bold;");
            return;
        }

        Rectangle currentBounds = windowManager.getWindowBounds(window.getHwnd());
        if (currentBounds != null) {
            window.setBounds(currentBounds);
            windowStatusLabel.setText(String.format("0x%X (%dx%d)",
                    window.getHwndVal(), currentBounds.width, currentBounds.height));
            windowStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #34d399; -fx-font-weight: bold;");
        } else {
            windowStatusLabel.setText(I18n.get("lbl.window_not_found"));
            windowStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f43f5e; -fx-font-weight: bold;");
        }
    }

    private Rectangle getActiveWindowBounds() {
        WindowInfo w = windowComboBox.getValue();
        if (w != null) {
            Rectangle b = windowManager.getWindowBounds(w.getHwnd());
            if (b != null) {
                w.setBounds(b);
                return b;
            }
        }
        return null;
    }

    private void updateEngineStateUI(AutomationState state) {
        engineStatusBadge.setText(state.name());
        engineStatusBadge.getStyleClass().removeAll("badge-idle", "badge-running", "badge-checking", "badge-stopping");

        switch (state) {
            case IDLE, STOPPED -> {
                engineStatusBadge.getStyleClass().add("badge-idle");
                startButton.setDisable(false);
                stopButton.setDisable(true);
                repeatCountField.setDisable(false);
                backgroundClickCheckBox.setDisable(false);
                loopCounterLabel.setText("Idle");
            }
            case EXECUTING_INITIAL_ACTIONS -> {
                ActionTargetMarkerOverlay.hide();
                engineStatusBadge.getStyleClass().add("badge-running");
                startButton.setDisable(true);
                stopButton.setDisable(false);
                repeatCountField.setDisable(true);
                backgroundClickCheckBox.setDisable(true);
                loopCounterLabel.setText("Running Initial Actions...");
            }
            case CHECKING_IMAGE -> {
                ActionTargetMarkerOverlay.hide();
                engineStatusBadge.getStyleClass().add("badge-checking");
                startButton.setDisable(true);
                stopButton.setDisable(false);
                repeatCountField.setDisable(true);
                backgroundClickCheckBox.setDisable(true);
                loopCounterLabel.setText("Checking image...");
            }
            case EXECUTING_BRANCH_A -> {
                ActionTargetMarkerOverlay.hide();
                engineStatusBadge.getStyleClass().add("badge-running");
                startButton.setDisable(true);
                stopButton.setDisable(false);
                repeatCountField.setDisable(true);
                backgroundClickCheckBox.setDisable(true);
                loopCounterLabel.setText("Running Branch A (MATCH)");
            }
            case EXECUTING_BRANCH_B -> {
                ActionTargetMarkerOverlay.hide();
                engineStatusBadge.getStyleClass().add("badge-running");
                startButton.setDisable(true);
                stopButton.setDisable(false);
                repeatCountField.setDisable(true);
                backgroundClickCheckBox.setDisable(true);
                loopCounterLabel.setText("Running Branch B (NOT MATCH)");
            }
            case STOPPING -> {
                engineStatusBadge.getStyleClass().add("badge-stopping");
                startButton.setDisable(true);
                stopButton.setDisable(true);
                repeatCountField.setDisable(true);
                backgroundClickCheckBox.setDisable(true);
                loopCounterLabel.setText("Stopping engine...");
            }
            case ERROR -> {
                engineStatusBadge.getStyleClass().add("badge-stopping");
                startButton.setDisable(false);
                stopButton.setDisable(true);
                repeatCountField.setDisable(false);
                backgroundClickCheckBox.setDisable(false);
                loopCounterLabel.setText("Engine Error");
            }
        }
    }

    @FXML
    private void handleRefreshWindows() {
        populateWindows();
        LoggerUtil.info("Window list refreshed.");
    }

    @FXML
    public void handleStart() {
        try {
            AutomationConfig config = buildConfigFromForm();
            automationEngine.start(config);
        } catch (Exception e) {
            showAlert("Cannot Start Automation", e.getMessage());
        }
    }

    @FXML
    public void handleStop() {
        automationEngine.stop();
    }

    // 1. Initial Actions Handlers
    @FXML
    private void handleAddActionInitial() {
        new ActionDialog(null, screenComboBox.getValue(), getActiveWindowBounds()).showAndWait().ifPresent(initialActions::add);
    }

    @FXML
    private void handleAddCheckImageInitial() {
        AutomationAction sampleCheck = new AutomationAction();
        sampleCheck.setType(AutomationActionType.CHECK_IMAGE);
        sampleCheck.setCoordinateMode(CoordinateMode.WINDOW_RELATIVE);
        sampleCheck.setX(100);
        sampleCheck.setY(100);
        sampleCheck.setWidth(300);
        sampleCheck.setHeight(200);
        sampleCheck.setThreshold(0.90);
        new ActionDialog(sampleCheck, screenComboBox.getValue(), getActiveWindowBounds()).showAndWait().ifPresent(initialActions::add);
    }

    @FXML
    private void handlePickCoordInitial() {
        CoordinatePickerOverlay.pick(screenComboBox.getValue(), getActiveWindowBounds(), CoordinateMode.WINDOW_RELATIVE, pt -> {
            AutomationAction action = new AutomationAction(AutomationActionType.CLICK, pt.x, pt.y, 300, CoordinateMode.WINDOW_RELATIVE);
            initialActions.add(action);
        });
    }

    @FXML
    private void handleEditActionInitial() {
        int idx = initialListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            new ActionDialog(initialActions.get(idx), screenComboBox.getValue(), getActiveWindowBounds())
                    .showAndWait().ifPresent(action -> initialActions.set(idx, action));
        }
    }

    @FXML
    private void handleDeleteActionInitial() {
        int idx = initialListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            initialActions.remove(idx);
        }
    }

    @FXML
    private void handleMoveUpInitial() {
        int idx = initialListView.getSelectionModel().getSelectedIndex();
        if (idx > 0) {
            Collections.swap(initialActions, idx, idx - 1);
            initialListView.getSelectionModel().select(idx - 1);
        }
    }

    @FXML
    private void handleMoveDownInitial() {
        int idx = initialListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < initialActions.size() - 1) {
            Collections.swap(initialActions, idx, idx + 1);
            initialListView.getSelectionModel().select(idx + 1);
        }
    }

    // 2. Branch A Handlers
    @FXML
    private void handleAddActionBranchA() {
        new ActionDialog(null, screenComboBox.getValue(), getActiveWindowBounds()).showAndWait().ifPresent(branchAActions::add);
    }

    @FXML
    private void handlePickCoordBranchA() {
        CoordinatePickerOverlay.pick(screenComboBox.getValue(), getActiveWindowBounds(), CoordinateMode.WINDOW_RELATIVE, pt -> {
            AutomationAction action = new AutomationAction(AutomationActionType.CLICK, pt.x, pt.y, 300, CoordinateMode.WINDOW_RELATIVE);
            branchAActions.add(action);
        });
    }

    @FXML
    private void handleEditActionBranchA() {
        int idx = branchAListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            new ActionDialog(branchAActions.get(idx), screenComboBox.getValue(), getActiveWindowBounds())
                    .showAndWait().ifPresent(action -> branchAActions.set(idx, action));
        }
    }

    @FXML
    private void handleDeleteActionBranchA() {
        int idx = branchAListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            branchAActions.remove(idx);
        }
    }

    @FXML
    private void handleMoveUpBranchA() {
        int idx = branchAListView.getSelectionModel().getSelectedIndex();
        if (idx > 0) {
            Collections.swap(branchAActions, idx, idx - 1);
            branchAListView.getSelectionModel().select(idx - 1);
        }
    }

    @FXML
    private void handleMoveDownBranchA() {
        int idx = branchAListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < branchAActions.size() - 1) {
            Collections.swap(branchAActions, idx, idx + 1);
            branchAListView.getSelectionModel().select(idx + 1);
        }
    }

    // 3. Branch B Handlers
    @FXML
    private void handleAddActionBranchB() {
        new ActionDialog(null, screenComboBox.getValue(), getActiveWindowBounds()).showAndWait().ifPresent(branchBActions::add);
    }

    @FXML
    private void handlePickCoordBranchB() {
        CoordinatePickerOverlay.pick(screenComboBox.getValue(), getActiveWindowBounds(), CoordinateMode.WINDOW_RELATIVE, pt -> {
            AutomationAction action = new AutomationAction(AutomationActionType.CLICK, pt.x, pt.y, 300, CoordinateMode.WINDOW_RELATIVE);
            branchBActions.add(action);
        });
    }

    @FXML
    private void handleEditActionBranchB() {
        int idx = branchBListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            new ActionDialog(branchBActions.get(idx), screenComboBox.getValue(), getActiveWindowBounds())
                    .showAndWait().ifPresent(action -> branchBActions.set(idx, action));
        }
    }

    @FXML
    private void handleDeleteActionBranchB() {
        int idx = branchBListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            branchBActions.remove(idx);
        }
    }

    @FXML
    private void handleMoveUpBranchB() {
        int idx = branchBListView.getSelectionModel().getSelectedIndex();
        if (idx > 0) {
            Collections.swap(branchBActions, idx, idx - 1);
            branchBListView.getSelectionModel().select(idx - 1);
        }
    }

    @FXML
    private void handleMoveDownBranchB() {
        int idx = branchBListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < branchBActions.size() - 1) {
            Collections.swap(branchBActions, idx, idx + 1);
            branchBListView.getSelectionModel().select(idx + 1);
        }
    }

    @FXML
    private void handleSaveProfile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.get("btn.save_profile"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Profile (*.json)", "*.json"));
        chooser.setInitialFileName("fco-profile.json");
        File file = chooser.showSaveDialog(startButton.getScene().getWindow());
        if (file != null) {
            try {
                AutomationConfig config = buildConfigFromForm();
                configRepository.save(config, file);
                LoggerUtil.info("Profile saved to: " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert("Save Failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleLoadProfile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.get("btn.load_profile"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Profile (*.json)", "*.json"));
        File file = chooser.showOpenDialog(startButton.getScene().getWindow());
        if (file != null) {
            try {
                AutomationConfig config = configRepository.load(file);
                applyConfigToForm(config);
                LoggerUtil.info("Profile loaded from: " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert("Load Failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleClearLog() {
        if (logArea != null) {
            logArea.clear();
        }
    }

    private AutomationConfig buildConfigFromForm() {
        AutomationConfig config = new AutomationConfig();
        ScreenInfo selectedScreen = screenComboBox.getValue();
        if (selectedScreen != null) {
            config.setTargetScreen(selectedScreen.getIndex());
        }

        WindowInfo selectedWindow = windowComboBox.getValue();
        if (selectedWindow != null) {
            config.setTargetWindowTitle(selectedWindow.getTitle());
        }

        int repeatCount = 0;
        try {
            String txt = repeatCountField.getText() != null ? repeatCountField.getText().trim() : "";
            if (!txt.isEmpty()) {
                repeatCount = Math.max(0, Integer.parseInt(txt));
            }
        } catch (NumberFormatException ignored) {
        }
        config.setRepeatCount(repeatCount);
        config.setBackgroundClick(backgroundClickCheckBox.isSelected());

        config.setInitialActions(new ArrayList<>(initialActions));
        config.setBranchA(new ArrayList<>(branchAActions));
        config.setBranchB(new ArrayList<>(branchBActions));
        return config;
    }

    private void applyConfigToForm(AutomationConfig config) {
        if (config == null) return;

        if (config.getTargetScreen() >= 0 && config.getTargetScreen() < screenComboBox.getItems().size()) {
            screenComboBox.getSelectionModel().select(config.getTargetScreen());
        }

        if (config.getTargetWindowTitle() != null && !config.getTargetWindowTitle().isBlank()) {
            for (WindowInfo w : windowComboBox.getItems()) {
                if (w.getTitle().equalsIgnoreCase(config.getTargetWindowTitle())) {
                    windowComboBox.getSelectionModel().select(w);
                    break;
                }
            }
        }

        repeatCountField.setText(String.valueOf(config.getRepeatCount()));
        backgroundClickCheckBox.setSelected(config.isBackgroundClick());

        initialActions.setAll(config.getInitialActions() != null ? config.getInitialActions() : Collections.emptyList());
        branchAActions.setAll(config.getBranchA() != null ? config.getBranchA() : Collections.emptyList());
        branchBActions.setAll(config.getBranchB() != null ? config.getBranchB() : Collections.emptyList());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public AutomationEngine getAutomationEngine() {
        return automationEngine;
    }
}
