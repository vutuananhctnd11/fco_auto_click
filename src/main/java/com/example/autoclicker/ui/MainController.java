package com.example.autoclicker.ui;

import com.example.autoclicker.automation.*;
import com.example.autoclicker.config.AutomationConfig;
import com.example.autoclicker.config.JsonConfigRepository;
import com.example.autoclicker.image.ImageDetector;
import com.example.autoclicker.image.ScreenshotService;
import com.example.autoclicker.screen.ScreenInfo;
import com.example.autoclicker.screen.ScreenManager;
import com.example.autoclicker.util.I18n;
import com.example.autoclicker.util.LoggerUtil;
import com.example.autoclicker.window.WindowInfo;
import com.example.autoclicker.window.WindowManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.awt.Rectangle;
import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main UI Controller for FCOAutoClick with full bilingual i18n support, Toolbar controls,
 * and dynamic multi-level CHECK_IMAGE branching (Initial -> A/B -> C/D/E/F).
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

    // Top Brand Bar Controls (Inline)
    @FXML private MenuButton optionsMenuButton;
    @FXML private MenuItem openProfileMenuItem;
    @FXML private MenuItem saveProfileMenuItem;
    @FXML private Menu languageSubMenu;
    @FXML private RadioMenuItem langEnMenuItem;
    @FXML private RadioMenuItem langViMenuItem;

    // Window & Screen Controls
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

    // Main Actions Workspace (TabPane)
    @FXML private TabPane branchTabPane;

    // 1. Initial Actions
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

    // 2. Branch A (Level 1 MATCH)
    @FXML private Tab branchATab;
    @FXML private Label branchATabDesc;
    @FXML private ListView<AutomationAction> branchAListView;
    @FXML private Button addBranchABtn;
    @FXML private Button addCheckImageBranchABtn;
    @FXML private Button pickCoordBranchABtn;
    @FXML private Button editBranchABtn;
    @FXML private Button delBranchABtn;
    @FXML private Button upBranchABtn;
    @FXML private Button downBranchABtn;

    // 3. Branch C (Level 2 MATCH from Branch A)
    @FXML private Tab branchCTab;
    @FXML private Label branchCTabDesc;
    @FXML private ListView<AutomationAction> branchCListView;
    @FXML private Button addBranchCBtn;
    @FXML private Button pickCoordBranchCBtn;
    @FXML private Button editBranchCBtn;
    @FXML private Button delBranchCBtn;
    @FXML private Button upBranchCBtn;
    @FXML private Button downBranchCBtn;

    // 4. Branch D (Level 2 NOT MATCH from Branch A)
    @FXML private Tab branchDTab;
    @FXML private Label branchDTabDesc;
    @FXML private ListView<AutomationAction> branchDListView;
    @FXML private Button addBranchDBtn;
    @FXML private Button pickCoordBranchDBtn;
    @FXML private Button editBranchDBtn;
    @FXML private Button delBranchDBtn;
    @FXML private Button upBranchDBtn;
    @FXML private Button downBranchDBtn;

    // 5. Branch B (Level 1 NOT MATCH)
    @FXML private Tab branchBTab;
    @FXML private Label branchBTabDesc;
    @FXML private ListView<AutomationAction> branchBListView;
    @FXML private Button addBranchBBtn;
    @FXML private Button addCheckImageBranchBBtn;
    @FXML private Button pickCoordBranchBBtn;
    @FXML private Button editBranchBBtn;
    @FXML private Button delBranchBBtn;
    @FXML private Button upBranchBBtn;
    @FXML private Button downBranchBBtn;

    // 6. Branch E (Level 2 MATCH from Branch B)
    @FXML private Tab branchETab;
    @FXML private Label branchETabDesc;
    @FXML private ListView<AutomationAction> branchEListView;
    @FXML private Button addBranchEBtn;
    @FXML private Button pickCoordBranchEBtn;
    @FXML private Button editBranchEBtn;
    @FXML private Button delBranchEBtn;
    @FXML private Button upBranchEBtn;
    @FXML private Button downBranchEBtn;

    // 7. Branch F (Level 2 NOT MATCH from Branch B)
    @FXML private Tab branchFTab;
    @FXML private Label branchFTabDesc;
    @FXML private ListView<AutomationAction> branchFListView;
    @FXML private Button addBranchFBtn;
    @FXML private Button pickCoordBranchFBtn;
    @FXML private Button editBranchFBtn;
    @FXML private Button delBranchFBtn;
    @FXML private Button upBranchFBtn;
    @FXML private Button downBranchFBtn;

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
    private final ObservableList<AutomationAction> branchCActions = FXCollections.observableArrayList();
    private final ObservableList<AutomationAction> branchDActions = FXCollections.observableArrayList();
    private final ObservableList<AutomationAction> branchEActions = FXCollections.observableArrayList();
    private final ObservableList<AutomationAction> branchFActions = FXCollections.observableArrayList();

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
        branchCListView.setItems(branchCActions);
        branchDListView.setItems(branchDActions);
        branchEListView.setItems(branchEActions);
        branchFListView.setItems(branchFActions);

        // Custom Cell Factory for beautiful action items
        initialListView.setCellFactory(lv -> new ActionListCell());
        branchAListView.setCellFactory(lv -> new ActionListCell());
        branchBListView.setCellFactory(lv -> new ActionListCell());
        branchCListView.setCellFactory(lv -> new ActionListCell());
        branchDListView.setCellFactory(lv -> new ActionListCell());
        branchEListView.setCellFactory(lv -> new ActionListCell());
        branchFListView.setCellFactory(lv -> new ActionListCell());

        // Double-click to Edit Action
        setupDoubleClickEdit(initialListView, this::handleEditActionInitial);
        setupDoubleClickEdit(branchAListView, this::handleEditActionBranchA);
        setupDoubleClickEdit(branchBListView, this::handleEditActionBranchB);
        setupDoubleClickEdit(branchCListView, this::handleEditActionBranchC);
        setupDoubleClickEdit(branchDListView, this::handleEditActionBranchD);
        setupDoubleClickEdit(branchEListView, this::handleEditActionBranchE);
        setupDoubleClickEdit(branchFListView, this::handleEditActionBranchF);

        // Setup On-screen Target Marker on Action Selection
        setupSelectionMarker(initialListView);
        setupSelectionMarker(branchAListView);
        setupSelectionMarker(branchBListView);
        setupSelectionMarker(branchCListView);
        setupSelectionMarker(branchDListView);
        setupSelectionMarker(branchEListView);
        setupSelectionMarker(branchFListView);

        // Dynamic tabs: show C & D only if Branch A has CHECK_IMAGE, show E & F only if Branch B has CHECK_IMAGE
        branchAActions.addListener((ListChangeListener<AutomationAction>) c -> updateDynamicBranchTabs());
        branchBActions.addListener((ListChangeListener<AutomationAction>) c -> updateDynamicBranchTabs());
        updateDynamicBranchTabs();

        // Hide marker and clear selection when switching tabs
        branchTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            clearAllActionSelections();
            ActionTargetMarkerOverlay.hide();
        });

        // Toggle marker visibility
        showMarkerCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                ActionTargetMarkerOverlay.hide();
            } else {
                showMarkerForCurrentTab();
            }
        });

        // Hide target marker overlay when main window is minimized to taskbar
        Platform.runLater(() -> {
            if (branchTabPane.getScene() != null && branchTabPane.getScene().getWindow() instanceof javafx.stage.Stage stage) {
                stage.iconifiedProperty().addListener((obs, oldVal, isIconified) -> {
                    if (isIconified) {
                        ActionTargetMarkerOverlay.hide();
                    } else {
                        showMarkerForCurrentTab();
                    }
                });
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
                    result.isMatched() ? "MATCH" : "NOT MATCH"));
        }));

        // Log Listener
        LoggerUtil.addLogListener(msg -> Platform.runLater(() -> {
            if (logArea != null) {
                logArea.appendText(msg + "\n");
            }
        }));

        applyI18n();
        boolean isElevated = windowManager.isElevated();
        if (isElevated) {
            LoggerUtil.info("Privilege Status: Elevated (Administrator). Full input access granted.");
        } else {
            LoggerUtil.warn("[LƯU Ý] FCOAutoClick chưa được chạy với quyền Administrator! Khi thao tác với game FC Online, hãy chạy ứng dụng bằng 'Run as administrator' để Windows không chặn chuột/phím.");
        }
        LoggerUtil.info("FCOAutoClick v1.0.0 [TunEngDev2003] initialized. Ready.");
    }

    /**
     * Dynamically displays Branch C and D only if Branch A has a CHECK_IMAGE action.
     * Dynamically displays Branch E and F only if Branch B has a CHECK_IMAGE action.
     */
    private void updateDynamicBranchTabs() {
        boolean hasCheckInA = hasCheckImageAction(branchAActions);
        boolean hasCheckInB = hasCheckImageAction(branchBActions);

        ObservableList<Tab> tabs = branchTabPane.getTabs();

        // 1. Branch C & D (sub-branches of A)
        if (hasCheckInA) {
            if (!tabs.contains(branchCTab)) {
                int idxA = tabs.indexOf(branchATab);
                if (idxA >= 0) {
                    tabs.add(idxA + 1, branchCTab);
                    tabs.add(idxA + 2, branchDTab);
                } else {
                    tabs.add(branchCTab);
                    tabs.add(branchDTab);
                }
            }
        } else {
            if (tabs.contains(branchCTab)) {
                Tab current = branchTabPane.getSelectionModel().getSelectedItem();
                if (current == branchCTab || current == branchDTab) {
                    branchTabPane.getSelectionModel().select(branchATab);
                }
                tabs.remove(branchCTab);
                tabs.remove(branchDTab);
            }
        }

        // 2. Branch E & F (sub-branches of B)
        if (hasCheckInB) {
            if (!tabs.contains(branchETab)) {
                int idxB = tabs.indexOf(branchBTab);
                if (idxB >= 0) {
                    tabs.add(idxB + 1, branchETab);
                    tabs.add(idxB + 2, branchFTab);
                } else {
                    tabs.add(branchETab);
                    tabs.add(branchFTab);
                }
            }
        } else {
            if (tabs.contains(branchETab)) {
                Tab current = branchTabPane.getSelectionModel().getSelectedItem();
                if (current == branchETab || current == branchFTab) {
                    branchTabPane.getSelectionModel().select(branchBTab);
                }
                tabs.remove(branchETab);
                tabs.remove(branchFTab);
            }
        }
    }

    private boolean hasCheckImageAction(List<AutomationAction> list) {
        if (list == null) return false;
        for (AutomationAction a : list) {
            if (a.getType() == AutomationActionType.CHECK_IMAGE) {
                return true;
            }
        }
        return false;
    }

    private void setupDoubleClickEdit(ListView<AutomationAction> listView, Runnable editAction) {
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                editAction.run();
            }
        });
    }

    private void clearAllActionSelections() {
        if (initialListView != null) initialListView.getSelectionModel().clearSelection();
        if (branchAListView != null) branchAListView.getSelectionModel().clearSelection();
        if (branchBListView != null) branchBListView.getSelectionModel().clearSelection();
        if (branchCListView != null) branchCListView.getSelectionModel().clearSelection();
        if (branchDListView != null) branchDListView.getSelectionModel().clearSelection();
        if (branchEListView != null) branchEListView.getSelectionModel().clearSelection();
        if (branchFListView != null) branchFListView.getSelectionModel().clearSelection();
    }

    private void refreshAllListViews() {
        if (initialListView != null) initialListView.refresh();
        if (branchAListView != null) branchAListView.refresh();
        if (branchBListView != null) branchBListView.refresh();
        if (branchCListView != null) branchCListView.refresh();
        if (branchDListView != null) branchDListView.refresh();
        if (branchEListView != null) branchEListView.refresh();
        if (branchFListView != null) branchFListView.refresh();
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

        // 1. Initial Tab
        initialTab.setText(I18n.get("tab.initial_actions"));
        initialTabDesc.setText(I18n.get("tab.initial_desc"));
        addInitialBtn.setText(I18n.get("btn.add_action"));
        addCheckImageBtn.setText(I18n.get("btn.add_check_image_initial"));
        pickCoordInitialBtn.setText(I18n.get("btn.pick_coord"));
        editInitialBtn.setText(I18n.get("btn.edit"));
        delInitialBtn.setText(I18n.get("btn.delete"));
        upInitialBtn.setText(I18n.get("btn.move_up"));
        downInitialBtn.setText(I18n.get("btn.move_down"));

        // 2. Branch A
        branchATab.setText(I18n.get("tab.branch_a"));
        branchATabDesc.setText(I18n.get("tab.branch_a_desc"));
        addBranchABtn.setText(I18n.get("btn.add_action"));
        addCheckImageBranchABtn.setText(I18n.get("btn.add_check_image_a"));
        pickCoordBranchABtn.setText(I18n.get("btn.pick_coord"));
        editBranchABtn.setText(I18n.get("btn.edit"));
        delBranchABtn.setText(I18n.get("btn.delete"));
        upBranchABtn.setText(I18n.get("btn.move_up"));
        downBranchABtn.setText(I18n.get("btn.move_down"));

        // 3. Branch C
        branchCTab.setText(I18n.get("tab.branch_c"));
        branchCTabDesc.setText(I18n.get("tab.branch_c_desc"));
        addBranchCBtn.setText(I18n.get("btn.add_action"));
        pickCoordBranchCBtn.setText(I18n.get("btn.pick_coord"));
        editBranchCBtn.setText(I18n.get("btn.edit"));
        delBranchCBtn.setText(I18n.get("btn.delete"));
        upBranchCBtn.setText(I18n.get("btn.move_up"));
        downBranchCBtn.setText(I18n.get("btn.move_down"));

        // 4. Branch D
        branchDTab.setText(I18n.get("tab.branch_d"));
        branchDTabDesc.setText(I18n.get("tab.branch_d_desc"));
        addBranchDBtn.setText(I18n.get("btn.add_action"));
        pickCoordBranchDBtn.setText(I18n.get("btn.pick_coord"));
        editBranchDBtn.setText(I18n.get("btn.edit"));
        delBranchDBtn.setText(I18n.get("btn.delete"));
        upBranchDBtn.setText(I18n.get("btn.move_up"));
        downBranchDBtn.setText(I18n.get("btn.move_down"));

        // 5. Branch B
        branchBTab.setText(I18n.get("tab.branch_b"));
        branchBTabDesc.setText(I18n.get("tab.branch_b_desc"));
        addBranchBBtn.setText(I18n.get("btn.add_action"));
        addCheckImageBranchBBtn.setText(I18n.get("btn.add_check_image_b"));
        pickCoordBranchBBtn.setText(I18n.get("btn.pick_coord"));
        editBranchBBtn.setText(I18n.get("btn.edit"));
        delBranchBBtn.setText(I18n.get("btn.delete"));
        upBranchBBtn.setText(I18n.get("btn.move_up"));
        downBranchBBtn.setText(I18n.get("btn.move_down"));

        // 6. Branch E
        branchETab.setText(I18n.get("tab.branch_e"));
        branchETabDesc.setText(I18n.get("tab.branch_e_desc"));
        addBranchEBtn.setText(I18n.get("btn.add_action"));
        pickCoordBranchEBtn.setText(I18n.get("btn.pick_coord"));
        editBranchEBtn.setText(I18n.get("btn.edit"));
        delBranchEBtn.setText(I18n.get("btn.delete"));
        upBranchEBtn.setText(I18n.get("btn.move_up"));
        downBranchEBtn.setText(I18n.get("btn.move_down"));

        // 7. Branch F
        branchFTab.setText(I18n.get("tab.branch_f"));
        branchFTabDesc.setText(I18n.get("tab.branch_f_desc"));
        addBranchFBtn.setText(I18n.get("btn.add_action"));
        pickCoordBranchFBtn.setText(I18n.get("btn.pick_coord"));
        editBranchFBtn.setText(I18n.get("btn.edit"));
        delBranchFBtn.setText(I18n.get("btn.delete"));
        upBranchFBtn.setText(I18n.get("btn.move_up"));
        downBranchFBtn.setText(I18n.get("btn.move_down"));

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
        updateEngineStateUI(automationEngine.getState());
        refreshAllListViews();
    }

    private void setupSelectionMarker(ListView<AutomationAction> listView) {
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldAction, newAction) -> {
            showMarkerForAction(newAction);
        });
    }

    public void showMarkerForAction(AutomationAction action) {
        if (showMarkerCheckBox != null && !showMarkerCheckBox.isSelected()) {
            ActionTargetMarkerOverlay.hide();
            return;
        }
        if (action == null || action.getType() == AutomationActionType.WAIT 
                || action.getType() == AutomationActionType.KEY_PRESS 
                || action.getType() == AutomationActionType.STOP 
                || action.getType() == AutomationActionType.CHECK_IMAGE) {
            ActionTargetMarkerOverlay.hide();
            return;
        }
        Rectangle windowBounds = getActiveWindowBounds();
        int screenX = action.getX();
        int screenY = action.getY();
        if (action.getCoordinateMode() == CoordinateMode.WINDOW_RELATIVE && windowBounds != null) {
            screenX = windowBounds.x + action.getX();
            screenY = windowBounds.y + action.getY();
        }
        String label = action.getType().name() + " (" + action.getX() + ", " + action.getY() + ")";
        ActionTargetMarkerOverlay.show(screenX, screenY, label);
    }

    private void showMarkerForCurrentTab() {
        Tab currentTab = branchTabPane.getSelectionModel().getSelectedItem();
        if (currentTab == initialTab) showMarkerForAction(initialListView.getSelectionModel().getSelectedItem());
        else if (currentTab == branchATab) showMarkerForAction(branchAListView.getSelectionModel().getSelectedItem());
        else if (currentTab == branchCTab) showMarkerForAction(branchCListView.getSelectionModel().getSelectedItem());
        else if (currentTab == branchDTab) showMarkerForAction(branchDListView.getSelectionModel().getSelectedItem());
        else if (currentTab == branchBTab) showMarkerForAction(branchBListView.getSelectionModel().getSelectedItem());
        else if (currentTab == branchETab) showMarkerForAction(branchEListView.getSelectionModel().getSelectedItem());
        else if (currentTab == branchFTab) showMarkerForAction(branchFListView.getSelectionModel().getSelectedItem());
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
        } else if (!windows.isEmpty()) {
            windowComboBox.getSelectionModel().select(0);
        }
        updateWindowStatus(windowComboBox.getValue());
    }

    private void updateWindowStatus(WindowInfo window) {
        if (window != null && windowManager.isWindowValid(window.getHwnd())) {
            windowStatusLabel.setText("Active: " + window.getTitle());
            windowStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #34d399; -fx-font-weight: bold;");

            // Automatically detect which screen contains this window
            Rectangle bounds = windowManager.getWindowBounds(window.getHwnd());
            if (bounds != null && screenComboBox != null && !screenComboBox.getItems().isEmpty()) {
                int centerX = bounds.x + bounds.width / 2;
                int centerY = bounds.y + bounds.height / 2;
                for (ScreenInfo s : screenComboBox.getItems()) {
                    if (s.getBounds().contains(centerX, centerY)) {
                        if (screenComboBox.getValue() != s) {
                            screenComboBox.getSelectionModel().select(s);
                            LoggerUtil.info("Window \"" + window.getTitle() + "\" detected on " + s.getName() + " -> Auto-switched target screen.");
                        }
                        break;
                    }
                }
            }
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
        if (state == null) state = AutomationState.IDLE;
        engineStatusBadge.setText(state.name());
        engineStatusBadge.getStyleClass().removeAll("badge-idle", "badge-running", "badge-checking", "badge-stopping");

        switch (state) {
            case IDLE, STOPPED -> {
                engineStatusBadge.getStyleClass().add("badge-idle");
                startButton.setDisable(false);
                stopButton.setDisable(true);
                repeatCountField.setDisable(false);
                backgroundClickCheckBox.setDisable(false);
                loopCounterLabel.setText(I18n.get("status.idle"));
            }
            case EXECUTING_INITIAL_ACTIONS -> {
                ActionTargetMarkerOverlay.hide();
                engineStatusBadge.getStyleClass().add("badge-running");
                startButton.setDisable(true);
                stopButton.setDisable(false);
                repeatCountField.setDisable(true);
                backgroundClickCheckBox.setDisable(true);
                loopCounterLabel.setText(I18n.get("status.executing_initial"));
            }
            case CHECKING_IMAGE -> {
                ActionTargetMarkerOverlay.hide();
                engineStatusBadge.getStyleClass().add("badge-checking");
                startButton.setDisable(true);
                stopButton.setDisable(false);
                repeatCountField.setDisable(true);
                backgroundClickCheckBox.setDisable(true);
                loopCounterLabel.setText(I18n.get("status.checking_image"));
            }
            case EXECUTING_BRANCH_A -> {
                ActionTargetMarkerOverlay.hide();
                engineStatusBadge.getStyleClass().add("badge-running");
                startButton.setDisable(true);
                stopButton.setDisable(false);
                repeatCountField.setDisable(true);
                backgroundClickCheckBox.setDisable(true);
                loopCounterLabel.setText(I18n.get("status.executing_branch_a"));
            }
            case EXECUTING_BRANCH_B -> {
                ActionTargetMarkerOverlay.hide();
                engineStatusBadge.getStyleClass().add("badge-running");
                startButton.setDisable(true);
                stopButton.setDisable(false);
                repeatCountField.setDisable(true);
                backgroundClickCheckBox.setDisable(true);
                loopCounterLabel.setText(I18n.get("status.executing_branch_b"));
            }
            case EXECUTING_BRANCH_C -> {
                ActionTargetMarkerOverlay.hide();
                engineStatusBadge.getStyleClass().add("badge-running");
                startButton.setDisable(true);
                stopButton.setDisable(false);
                repeatCountField.setDisable(true);
                backgroundClickCheckBox.setDisable(true);
                loopCounterLabel.setText(I18n.get("status.executing_branch_c"));
            }
            case EXECUTING_BRANCH_D -> {
                ActionTargetMarkerOverlay.hide();
                engineStatusBadge.getStyleClass().add("badge-running");
                startButton.setDisable(true);
                stopButton.setDisable(false);
                repeatCountField.setDisable(true);
                backgroundClickCheckBox.setDisable(true);
                loopCounterLabel.setText(I18n.get("status.executing_branch_d"));
            }
            case EXECUTING_BRANCH_E -> {
                ActionTargetMarkerOverlay.hide();
                engineStatusBadge.getStyleClass().add("badge-running");
                startButton.setDisable(true);
                stopButton.setDisable(false);
                repeatCountField.setDisable(true);
                backgroundClickCheckBox.setDisable(true);
                loopCounterLabel.setText(I18n.get("status.executing_branch_e"));
            }
            case EXECUTING_BRANCH_F -> {
                ActionTargetMarkerOverlay.hide();
                engineStatusBadge.getStyleClass().add("badge-running");
                startButton.setDisable(true);
                stopButton.setDisable(false);
                repeatCountField.setDisable(true);
                backgroundClickCheckBox.setDisable(true);
                loopCounterLabel.setText(I18n.get("status.executing_branch_f"));
            }
            case STOPPING -> {
                engineStatusBadge.getStyleClass().add("badge-stopping");
                startButton.setDisable(true);
                stopButton.setDisable(true);
                repeatCountField.setDisable(true);
                backgroundClickCheckBox.setDisable(true);
                loopCounterLabel.setText(I18n.get("status.stopping"));
            }
            case ERROR -> {
                engineStatusBadge.getStyleClass().add("badge-stopping");
                startButton.setDisable(false);
                stopButton.setDisable(true);
                repeatCountField.setDisable(false);
                backgroundClickCheckBox.setDisable(false);
                loopCounterLabel.setText(I18n.get("status.error"));
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
        ActionTargetMarkerOverlay.hide();
        try {
            if (!windowManager.isElevated()) {
                LoggerUtil.warn("Cảnh báo: Ứng dụng chưa chạy quyền Administrator. Game FC Online có thể chặn phím/chuột do bảo mật Windows UIPI.");
            }
            if (backgroundClickCheckBox.isSelected()) {
                LoggerUtil.warn("Lưu ý: Chế độ 'Chạy ngầm (PostMessage)' không hỗ trợ game DirectX/FCO. Nếu game không nhận chuột, vui lòng bỏ chọn Background Click.");
            }
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

    // =========================================================================
    // 1. Initial Actions Handlers
    // =========================================================================
    @FXML
    private void handleAddActionInitial() {
        new ActionDialog(null, screenComboBox.getValue(), getActiveWindowBounds()).showAndWait().ifPresent(initialActions::add);
    }

    @FXML
    private void handleAddCheckImageInitial() {
        AutomationAction sampleCheck = createDefaultCheckImage();
        new ActionDialog(sampleCheck, screenComboBox.getValue(), getActiveWindowBounds()).showAndWait().ifPresent(initialActions::add);
    }

    @FXML
    private void handlePickCoordInitial() {
        CoordinatePickerOverlay.pick(screenComboBox.getValue(), getActiveWindowBounds(), CoordinateMode.WINDOW_RELATIVE, pt -> {
            AutomationAction action = new AutomationAction(AutomationActionType.CLICK, pt.x, pt.y, 300, CoordinateMode.WINDOW_RELATIVE);
            initialActions.add(action);
        });
    }

    private void editSelectedAction(ListView<AutomationAction> listView, ObservableList<AutomationAction> actionList, Runnable afterChange) {
        int idx = listView.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < actionList.size()) {
            new ActionDialog(actionList.get(idx), screenComboBox.getValue(), getActiveWindowBounds())
                    .showAndWait().ifPresent(action -> {
                        actionList.set(idx, action);
                        if (afterChange != null) {
                            afterChange.run();
                        }
                    });
            showMarkerForAction(listView.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    private void handleEditActionInitial() {
        editSelectedAction(initialListView, initialActions, null);
    }

    private void deleteSelectedAction(ListView<AutomationAction> listView, ObservableList<AutomationAction> actionList, Runnable afterChange) {
        int idx = listView.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < actionList.size()) {
            actionList.remove(idx);
            if (actionList.isEmpty()) {
                ActionTargetMarkerOverlay.hide();
            } else {
                int nextIdx = Math.min(idx, actionList.size() - 1);
                listView.getSelectionModel().select(nextIdx);
                listView.scrollTo(nextIdx);
            }
            if (afterChange != null) {
                afterChange.run();
            }
        }
    }

    private void moveUpSelectedAction(ListView<AutomationAction> listView, ObservableList<AutomationAction> actionList) {
        int idx = listView.getSelectionModel().getSelectedIndex();
        if (idx > 0 && idx < actionList.size()) {
            Collections.swap(actionList, idx, idx - 1);
            listView.getSelectionModel().select(idx - 1);
            listView.scrollTo(idx - 1);
        }
    }

    private void moveDownSelectedAction(ListView<AutomationAction> listView, ObservableList<AutomationAction> actionList) {
        int idx = listView.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < actionList.size() - 1) {
            Collections.swap(actionList, idx, idx + 1);
            listView.getSelectionModel().select(idx + 1);
            listView.scrollTo(idx + 1);
        }
    }

    @FXML
    private void handleDeleteActionInitial() {
        deleteSelectedAction(initialListView, initialActions, null);
    }

    @FXML
    private void handleMoveUpInitial() {
        moveUpSelectedAction(initialListView, initialActions);
    }

    @FXML
    private void handleMoveDownInitial() {
        moveDownSelectedAction(initialListView, initialActions);
    }

    // =========================================================================
    // 2. Branch A Handlers
    // =========================================================================
    @FXML
    private void handleAddActionBranchA() {
        new ActionDialog(null, screenComboBox.getValue(), getActiveWindowBounds()).showAndWait().ifPresent(branchAActions::add);
    }

    @FXML
    private void handleAddCheckImageBranchA() {
        AutomationAction sampleCheck = createDefaultCheckImage();
        new ActionDialog(sampleCheck, screenComboBox.getValue(), getActiveWindowBounds()).showAndWait().ifPresent(action -> {
            branchAActions.add(action);
            updateDynamicBranchTabs();
            branchTabPane.getSelectionModel().select(branchCTab);
        });
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
        editSelectedAction(branchAListView, branchAActions, this::updateDynamicBranchTabs);
    }

    @FXML
    private void handleDeleteActionBranchA() {
        deleteSelectedAction(branchAListView, branchAActions, this::updateDynamicBranchTabs);
    }

    @FXML
    private void handleMoveUpBranchA() {
        moveUpSelectedAction(branchAListView, branchAActions);
    }

    @FXML
    private void handleMoveDownBranchA() {
        moveDownSelectedAction(branchAListView, branchAActions);
    }

    // =========================================================================
    // 3. Branch C Handlers (A: MATCH)
    // =========================================================================
    @FXML
    private void handleAddActionBranchC() {
        new ActionDialog(null, screenComboBox.getValue(), getActiveWindowBounds()).showAndWait().ifPresent(branchCActions::add);
    }

    @FXML
    private void handlePickCoordBranchC() {
        CoordinatePickerOverlay.pick(screenComboBox.getValue(), getActiveWindowBounds(), CoordinateMode.WINDOW_RELATIVE, pt -> {
            AutomationAction action = new AutomationAction(AutomationActionType.CLICK, pt.x, pt.y, 300, CoordinateMode.WINDOW_RELATIVE);
            branchCActions.add(action);
        });
    }

    @FXML
    private void handleEditActionBranchC() {
        editSelectedAction(branchCListView, branchCActions, null);
    }

    @FXML
    private void handleDeleteActionBranchC() {
        deleteSelectedAction(branchCListView, branchCActions, null);
    }

    @FXML
    private void handleMoveUpBranchC() {
        moveUpSelectedAction(branchCListView, branchCActions);
    }

    @FXML
    private void handleMoveDownBranchC() {
        moveDownSelectedAction(branchCListView, branchCActions);
    }

    // =========================================================================
    // 4. Branch D Handlers (A: NOT MATCH)
    // =========================================================================
    @FXML
    private void handleAddActionBranchD() {
        new ActionDialog(null, screenComboBox.getValue(), getActiveWindowBounds()).showAndWait().ifPresent(branchDActions::add);
    }

    @FXML
    private void handlePickCoordBranchD() {
        CoordinatePickerOverlay.pick(screenComboBox.getValue(), getActiveWindowBounds(), CoordinateMode.WINDOW_RELATIVE, pt -> {
            AutomationAction action = new AutomationAction(AutomationActionType.CLICK, pt.x, pt.y, 300, CoordinateMode.WINDOW_RELATIVE);
            branchDActions.add(action);
        });
    }

    @FXML
    private void handleEditActionBranchD() {
        editSelectedAction(branchDListView, branchDActions, null);
    }

    @FXML
    private void handleDeleteActionBranchD() {
        deleteSelectedAction(branchDListView, branchDActions, null);
    }

    @FXML
    private void handleMoveUpBranchD() {
        moveUpSelectedAction(branchDListView, branchDActions);
    }

    @FXML
    private void handleMoveDownBranchD() {
        moveDownSelectedAction(branchDListView, branchDActions);
    }

    // =========================================================================
    // 5. Branch B Handlers
    // =========================================================================
    @FXML
    private void handleAddActionBranchB() {
        new ActionDialog(null, screenComboBox.getValue(), getActiveWindowBounds()).showAndWait().ifPresent(branchBActions::add);
    }

    @FXML
    private void handleAddCheckImageBranchB() {
        AutomationAction sampleCheck = createDefaultCheckImage();
        new ActionDialog(sampleCheck, screenComboBox.getValue(), getActiveWindowBounds()).showAndWait().ifPresent(action -> {
            branchBActions.add(action);
            updateDynamicBranchTabs();
            branchTabPane.getSelectionModel().select(branchETab);
        });
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
        editSelectedAction(branchBListView, branchBActions, this::updateDynamicBranchTabs);
    }

    @FXML
    private void handleDeleteActionBranchB() {
        deleteSelectedAction(branchBListView, branchBActions, this::updateDynamicBranchTabs);
    }

    @FXML
    private void handleMoveUpBranchB() {
        moveUpSelectedAction(branchBListView, branchBActions);
    }

    @FXML
    private void handleMoveDownBranchB() {
        moveDownSelectedAction(branchBListView, branchBActions);
    }

    // =========================================================================
    // 6. Branch E Handlers (B: MATCH)
    // =========================================================================
    @FXML
    private void handleAddActionBranchE() {
        new ActionDialog(null, screenComboBox.getValue(), getActiveWindowBounds()).showAndWait().ifPresent(branchEActions::add);
    }

    @FXML
    private void handlePickCoordBranchE() {
        CoordinatePickerOverlay.pick(screenComboBox.getValue(), getActiveWindowBounds(), CoordinateMode.WINDOW_RELATIVE, pt -> {
            AutomationAction action = new AutomationAction(AutomationActionType.CLICK, pt.x, pt.y, 300, CoordinateMode.WINDOW_RELATIVE);
            branchEActions.add(action);
        });
    }

    @FXML
    private void handleEditActionBranchE() {
        editSelectedAction(branchEListView, branchEActions, null);
    }

    @FXML
    private void handleDeleteActionBranchE() {
        deleteSelectedAction(branchEListView, branchEActions, null);
    }

    @FXML
    private void handleMoveUpBranchE() {
        moveUpSelectedAction(branchEListView, branchEActions);
    }

    @FXML
    private void handleMoveDownBranchE() {
        moveDownSelectedAction(branchEListView, branchEActions);
    }

    // =========================================================================
    // 7. Branch F Handlers (B: NOT MATCH)
    // =========================================================================
    @FXML
    private void handleAddActionBranchF() {
        new ActionDialog(null, screenComboBox.getValue(), getActiveWindowBounds()).showAndWait().ifPresent(branchFActions::add);
    }

    @FXML
    private void handlePickCoordBranchF() {
        CoordinatePickerOverlay.pick(screenComboBox.getValue(), getActiveWindowBounds(), CoordinateMode.WINDOW_RELATIVE, pt -> {
            AutomationAction action = new AutomationAction(AutomationActionType.CLICK, pt.x, pt.y, 300, CoordinateMode.WINDOW_RELATIVE);
            branchFActions.add(action);
        });
    }

    @FXML
    private void handleEditActionBranchF() {
        editSelectedAction(branchFListView, branchFActions, null);
    }

    @FXML
    private void handleDeleteActionBranchF() {
        deleteSelectedAction(branchFListView, branchFActions, null);
    }

    @FXML
    private void handleMoveUpBranchF() {
        moveUpSelectedAction(branchFListView, branchFActions);
    }

    @FXML
    private void handleMoveDownBranchF() {
        moveDownSelectedAction(branchFListView, branchFActions);
    }

    private AutomationAction createDefaultCheckImage() {
        AutomationAction sampleCheck = new AutomationAction();
        sampleCheck.setType(AutomationActionType.CHECK_IMAGE);
        sampleCheck.setCoordinateMode(CoordinateMode.WINDOW_RELATIVE);
        sampleCheck.setX(100);
        sampleCheck.setY(100);
        sampleCheck.setWidth(300);
        sampleCheck.setHeight(200);
        sampleCheck.setThreshold(0.90);
        return sampleCheck;
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
        config.setBranchC(new ArrayList<>(branchCActions));
        config.setBranchD(new ArrayList<>(branchDActions));
        config.setBranchE(new ArrayList<>(branchEActions));
        config.setBranchF(new ArrayList<>(branchFActions));
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
        branchCActions.setAll(config.getBranchC() != null ? config.getBranchC() : Collections.emptyList());
        branchDActions.setAll(config.getBranchD() != null ? config.getBranchD() : Collections.emptyList());
        branchEActions.setAll(config.getBranchE() != null ? config.getBranchE() : Collections.emptyList());
        branchFActions.setAll(config.getBranchF() != null ? config.getBranchF() : Collections.emptyList());

        updateDynamicBranchTabs();
        refreshAllListViews();
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
