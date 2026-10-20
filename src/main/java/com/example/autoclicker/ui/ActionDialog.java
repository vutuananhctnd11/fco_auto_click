package com.example.autoclicker.ui;

import com.example.autoclicker.automation.AutomationAction;
import com.example.autoclicker.automation.AutomationActionType;
import com.example.autoclicker.automation.CoordinateMode;
import com.example.autoclicker.image.ImageDetector;
import com.example.autoclicker.image.MatchResult;
import com.example.autoclicker.image.ScreenshotService;
import com.example.autoclicker.screen.CaptureRegion;
import com.example.autoclicker.screen.ScreenInfo;
import com.example.autoclicker.util.I18n;
import com.example.autoclicker.util.LoggerUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;

/**
 * Universal Action Configuration Dialog supporting CLICK, MOVE, SCROLL, KEY_PRESS, WAIT, and CHECK_IMAGE.
 * Completely restyled with application-native Dark Theme and clean text without broken emoji icons.
 * Developed by TunEngDev2003.
 */
public class ActionDialog extends Dialog<AutomationAction> {

    private final ComboBox<AutomationActionType> typeCombo;
    private final ComboBox<CoordinateMode> modeCombo;
    private final TextField xField;
    private final TextField yField;
    private final TextField widthField;
    private final TextField heightField;
    private final TextField scrollField;
    private final TextField keyField;
    private final TextField delayField;
    private final Button pickCoordBtn;
    private final Button pickRegionBtn;
    private final TextField templatePathField;
    private final Button browseTemplateBtn;
    private final ImageView templatePreviewView;
    private final Label templateDimLabel;
    private final Slider thresholdSlider;
    private final TextField thresholdField;
    private final Button testImageBtn;
    private final Label testResultBadge;
    private final Label testConfidenceLabel;

    public ActionDialog(AutomationAction existingAction, ScreenInfo screen, Rectangle windowBounds) {
        setTitle(existingAction == null ? I18n.get("dialog.action.add_title") : I18n.get("dialog.action.edit_title"));
        setHeaderText(existingAction == null ? I18n.get("dialog.action.header_add") : I18n.get("dialog.action.header_edit"));

        initModality(Modality.APPLICATION_MODAL);
        DialogPane dialogPane = getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogPane.setPrefWidth(550);

        // Apply Dark Theme Stylesheet and inline styling for cohesive aesthetics
        if (getClass().getResource("/css/style.css") != null) {
            dialogPane.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        }
        dialogPane.setStyle("-fx-background-color: #141822; -fx-border-color: #272f40; -fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        // Set application icon on dialog stage
        dialogPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && newScene.getWindow() instanceof Stage stage) {
                try (InputStream iconStream = getClass().getResourceAsStream("/icons/FCOAutoClick_logo.png")) {
                    if (iconStream != null) {
                        stage.getIcons().add(new Image(iconStream));
                    }
                } catch (Exception ignored) {
                }
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 20, 16, 20));
        grid.setStyle("-fx-background-color: #141822;");

        String textFieldStyle = "-fx-background-color: #1e2638; -fx-text-fill: #ffffff; -fx-border-color: #3b4860; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-padding: 6px 10px;";
        String comboStyle = "-fx-background-color: #1e2638; -fx-border-color: #3b4860; -fx-border-radius: 4px; -fx-background-radius: 4px;";
        String btnPrimaryStyle = "-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-padding: 6px 14px; -fx-cursor: hand;";
        String btnSecondaryStyle = "-fx-background-color: #334155; -fx-text-fill: #e2e8f0; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-padding: 6px 14px; -fx-cursor: hand;";

        // Type ComboBox with localized StringConverter (No emoji icons in text)
        typeCombo = new ComboBox<>(FXCollections.observableArrayList(AutomationActionType.values()));
        typeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(AutomationActionType type) {
                if (type == null) return "";
                return I18n.get("action.type." + type.name().toLowerCase());
            }

            @Override
            public AutomationActionType fromString(String string) {
                return null;
            }
        });
        typeCombo.setValue(AutomationActionType.CLICK);
        typeCombo.setPrefWidth(280);
        typeCombo.setStyle(comboStyle);

        // Coordinate Mode ComboBox with localized StringConverter
        modeCombo = new ComboBox<>(FXCollections.observableArrayList(CoordinateMode.values()));
        modeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(CoordinateMode mode) {
                if (mode == null) return "";
                return I18n.get("coord.mode." + mode.name().toLowerCase());
            }

            @Override
            public CoordinateMode fromString(String string) {
                return null;
            }
        });
        modeCombo.setValue(CoordinateMode.WINDOW_RELATIVE);
        modeCombo.setPrefWidth(280);
        modeCombo.setStyle(comboStyle);

        xField = new TextField("0");
        xField.setStyle(textFieldStyle);
        yField = new TextField("0");
        yField.setStyle(textFieldStyle);
        widthField = new TextField("300");
        widthField.setStyle(textFieldStyle);
        heightField = new TextField("200");
        heightField.setStyle(textFieldStyle);
        scrollField = new TextField("-1");
        scrollField.setStyle(textFieldStyle);
        keyField = new TextField("ESC");
        keyField.setStyle(textFieldStyle);
        delayField = new TextField("300");
        delayField.setStyle(textFieldStyle);

        pickCoordBtn = new Button(I18n.get("dialog.action.pick_btn"));
        pickCoordBtn.setStyle(btnPrimaryStyle);

        pickRegionBtn = new Button(I18n.get("btn.pick_region"));
        pickRegionBtn.setStyle(btnPrimaryStyle);

        templatePathField = new TextField("");
        templatePathField.setPromptText(I18n.get("prompt.template_path"));
        templatePathField.setStyle(textFieldStyle);
        templatePathField.setPrefWidth(240);

        browseTemplateBtn = new Button(I18n.get("btn.browse"));
        browseTemplateBtn.setStyle(btnSecondaryStyle);

        templatePreviewView = new ImageView();
        templatePreviewView.setFitHeight(40);
        templatePreviewView.setFitWidth(100);
        templatePreviewView.setPreserveRatio(true);
        templateDimLabel = new Label(I18n.get("lbl.no_template"));
        templateDimLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        thresholdSlider = new Slider(0.10, 1.0, 0.90);
        thresholdSlider.setBlockIncrement(0.05);
        thresholdField = new TextField("0.90");
        thresholdField.setPrefWidth(60);
        thresholdField.setStyle(textFieldStyle);

        thresholdSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                thresholdField.setText(String.format(Locale.US, "%.2f", newVal.doubleValue())));
        thresholdField.setOnAction(e -> {
            try {
                double val = Double.parseDouble(thresholdField.getText().trim());
                if (val >= 0.1 && val <= 1.0) thresholdSlider.setValue(val);
            } catch (Exception ignored) {
            }
        });

        testImageBtn = new Button(I18n.get("btn.test_image"));
        testImageBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-padding: 6px 14px; -fx-cursor: hand;");
        testResultBadge = new Label(I18n.get("lbl.untested"));
        testResultBadge.setStyle("-fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        testConfidenceLabel = new Label(MessageFormat.format(I18n.get("lbl.confidence"), "--"));
        testConfidenceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

        // Load existing values
        if (existingAction != null) {
            typeCombo.setValue(existingAction.getType() != null ? existingAction.getType() : AutomationActionType.CLICK);
            modeCombo.setValue(existingAction.getCoordinateMode() != null ? existingAction.getCoordinateMode() : CoordinateMode.WINDOW_RELATIVE);
            xField.setText(String.valueOf(existingAction.getX()));
            yField.setText(String.valueOf(existingAction.getY()));
            widthField.setText(String.valueOf(existingAction.getWidth() > 0 ? existingAction.getWidth() : 300));
            heightField.setText(String.valueOf(existingAction.getHeight() > 0 ? existingAction.getHeight() : 200));
            scrollField.setText(String.valueOf(existingAction.getScrollAmount() != 0 ? existingAction.getScrollAmount() : -1));
            keyField.setText(existingAction.getKeyName() != null ? existingAction.getKeyName() : "");
            delayField.setText(String.valueOf(existingAction.getDelayMs()));
            templatePathField.setText(existingAction.getTemplatePath() != null ? existingAction.getTemplatePath() : "");
            thresholdSlider.setValue(existingAction.getThreshold() > 0 ? existingAction.getThreshold() : 0.90);
            updatePreview(existingAction.getTemplatePath());
        }

        templatePathField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview(newVal));

        // Browse Template
        browseTemplateBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(I18n.get("lbl.template_image"));
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files (*.png, *.jpg, *.jpeg, *.bmp)", "*.png", "*.jpg", "*.jpeg", "*.bmp"));
            File file = chooser.showOpenDialog(getDialogPane().getScene().getWindow());
            if (file != null) {
                templatePathField.setText(file.getAbsolutePath());
            }
        });

        // Pick Coordinate
        pickCoordBtn.setOnAction(e -> {
            CoordinatePickerOverlay.pick(screen, windowBounds, modeCombo.getValue(), pt -> {
                xField.setText(String.valueOf(pt.x));
                yField.setText(String.valueOf(pt.y));
            });
        });

        // Pick Region
        pickRegionBtn.setOnAction(e -> {
            RegionSelectionOverlay.show(screen, windowBounds, modeCombo.getValue(), reg -> {
                xField.setText(String.valueOf(reg.getX()));
                yField.setText(String.valueOf(reg.getY()));
                widthField.setText(String.valueOf(reg.getWidth()));
                heightField.setText(String.valueOf(reg.getHeight()));
                modeCombo.setValue(reg.getCoordinateMode());
            });
        });

        // Test Image
        testImageBtn.setOnAction(e -> {
            String path = templatePathField.getText().trim();
            if (path.isEmpty()) return;
            try {
                int x = Integer.parseInt(xField.getText().trim());
                int y = Integer.parseInt(yField.getText().trim());
                int w = Integer.parseInt(widthField.getText().trim());
                int h = Integer.parseInt(heightField.getText().trim());
                CaptureRegion region = new CaptureRegion(x, y, w, h, modeCombo.getValue());

                ScreenshotService ss = new ScreenshotService();
                ImageDetector id = new ImageDetector();
                BufferedImage tpl = id.loadTemplate(path);
                BufferedImage shot = ss.capture(screen, region, windowBounds);
                MatchResult res = id.match(shot, tpl, thresholdSlider.getValue());

                testConfidenceLabel.setText(MessageFormat.format(I18n.get("lbl.confidence"),
                        String.format(Locale.US, "%.2f", res.getConfidence())));
                if (res.isMatched()) {
                    testResultBadge.setText(I18n.get("lbl.match"));
                    testResultBadge.setStyle("-fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
                } else {
                    testResultBadge.setText(I18n.get("lbl.not_match"));
                    testResultBadge.setStyle("-fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-background-color: #f97316; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
                }
            } catch (Exception ex) {
                LoggerUtil.error("Test image failed in dialog: " + ex.getMessage());
            }
        });

        // Layout construction
        Label typeLbl = createStyledLabel(I18n.get("dialog.action.type"));
        Label modeLbl = createStyledLabel(I18n.get("dialog.action.mode"));
        Label xLbl = createStyledLabel(I18n.get("dialog.action.x"));
        Label yLbl = createStyledLabel(I18n.get("dialog.action.y"));
        Label wLbl = createStyledLabel(I18n.get("dialog.action.width"));
        Label hLbl = createStyledLabel(I18n.get("dialog.action.height"));
        Label scrollLbl = createStyledLabel(I18n.get("dialog.action.scroll_amount"));
        Label keyLbl = createStyledLabel(I18n.get("dialog.action.key"));
        Label delayLbl = createStyledLabel(I18n.get("dialog.action.delay"));
        Label tplLbl = createStyledLabel(I18n.get("lbl.template_image"));
        Label threshLbl = createStyledLabel(I18n.get("lbl.threshold"));

        grid.add(typeLbl, 0, 0);
        grid.add(typeCombo, 1, 0);

        grid.add(modeLbl, 0, 1);
        grid.add(modeCombo, 1, 1);

        grid.add(xLbl, 0, 2);
        HBox xBox = new HBox(8, xField, pickCoordBtn);
        grid.add(xBox, 1, 2);

        grid.add(yLbl, 0, 3);
        grid.add(yField, 1, 3);

        grid.add(wLbl, 0, 4);
        grid.add(widthField, 1, 4);

        grid.add(hLbl, 0, 5);
        HBox hBox = new HBox(8, heightField, pickRegionBtn);
        grid.add(hBox, 1, 5);

        grid.add(scrollLbl, 0, 6);
        Label scrollTipLbl = new Label(I18n.get("dialog.action.scroll_tip"));
        scrollTipLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        VBox scrollBox = new VBox(4, scrollField, scrollTipLbl);
        grid.add(scrollBox, 1, 6);

        grid.add(tplLbl, 0, 7);
        VBox tplBox = new VBox(6,
                new HBox(6, templatePathField, browseTemplateBtn),
                new HBox(8, templatePreviewView, templateDimLabel));
        grid.add(tplBox, 1, 7);

        grid.add(threshLbl, 0, 8);
        HBox threshBox = new HBox(8, thresholdSlider, thresholdField);
        grid.add(threshBox, 1, 8);

        Label testLbl = createStyledLabel(I18n.get("dialog.action.test_match"));
        HBox testBox = new HBox(8, testImageBtn, testResultBadge, testConfidenceLabel);
        grid.add(testLbl, 0, 9);
        grid.add(testBox, 1, 9);

        grid.add(keyLbl, 0, 10);
        grid.add(keyField, 1, 10);

        grid.add(delayLbl, 0, 11);
        grid.add(delayField, 1, 11);

        Label tipLbl = new Label(I18n.get("dialog.action.key_tip"));
        tipLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        grid.add(tipLbl, 1, 12);

        Runnable updateVisibility = () -> {
            AutomationActionType type = typeCombo.getValue();
            boolean isCheckImage = (type == AutomationActionType.CHECK_IMAGE);
            boolean isMouse = (type == AutomationActionType.CLICK || type == AutomationActionType.DOUBLE_CLICK || type == AutomationActionType.RIGHT_CLICK || type == AutomationActionType.MOVE || type == AutomationActionType.SCROLL);
            boolean isScroll = (type == AutomationActionType.SCROLL);
            boolean isKey = (type == AutomationActionType.KEY_PRESS);

            modeCombo.setDisable(!isMouse && !isCheckImage);
            xField.setDisable(!isMouse && !isCheckImage);
            yField.setDisable(!isMouse && !isCheckImage);
            pickCoordBtn.setVisible(isMouse);
            pickCoordBtn.setManaged(isMouse);

            wLbl.setVisible(isCheckImage);
            wLbl.setManaged(isCheckImage);
            widthField.setVisible(isCheckImage);
            widthField.setManaged(isCheckImage);

            hLbl.setVisible(isCheckImage);
            hLbl.setManaged(isCheckImage);
            hBox.setVisible(isCheckImage);
            hBox.setManaged(isCheckImage);

            scrollLbl.setVisible(isScroll);
            scrollLbl.setManaged(isScroll);
            scrollBox.setVisible(isScroll);
            scrollBox.setManaged(isScroll);

            tplLbl.setVisible(isCheckImage);
            tplLbl.setManaged(isCheckImage);
            tplBox.setVisible(isCheckImage);
            tplBox.setManaged(isCheckImage);

            threshLbl.setVisible(isCheckImage);
            threshLbl.setManaged(isCheckImage);
            threshBox.setVisible(isCheckImage);
            threshBox.setManaged(isCheckImage);

            testLbl.setVisible(isCheckImage);
            testLbl.setManaged(isCheckImage);
            testBox.setVisible(isCheckImage);
            testBox.setManaged(isCheckImage);

            keyLbl.setVisible(isKey);
            keyLbl.setManaged(isKey);
            keyField.setVisible(isKey);
            keyField.setManaged(isKey);
            tipLbl.setVisible(isKey);
            tipLbl.setManaged(isKey);

            delayLbl.setVisible(!isCheckImage);
            delayLbl.setManaged(!isCheckImage);
            delayField.setVisible(!isCheckImage);
            delayField.setManaged(!isCheckImage);
        };

        typeCombo.valueProperty().addListener((obs, o, n) -> updateVisibility.run());
        updateVisibility.run();

        dialogPane.setContent(grid);

        // Customize Dialog Buttons styling for dark aesthetic
        Platform.runLater(() -> {
            Button okBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
            if (okBtn != null) {
                okBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6px 22px; -fx-background-radius: 5px; -fx-cursor: hand;");
            }
            Button cancelBtn = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
            if (cancelBtn != null) {
                cancelBtn.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0; -fx-font-weight: bold; -fx-padding: 6px 20px; -fx-background-radius: 5px; -fx-cursor: hand;");
            }
        });

        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                AutomationAction action = new AutomationAction();
                AutomationActionType selectedType = typeCombo.getValue();
                if (selectedType == null) {
                    selectedType = AutomationActionType.CLICK;
                }
                action.setType(selectedType);

                CoordinateMode selectedMode = modeCombo.getValue();
                action.setCoordinateMode(selectedMode != null ? selectedMode : CoordinateMode.WINDOW_RELATIVE);

                try {
                    action.setX(Integer.parseInt(xField.getText().trim()));
                } catch (Exception ignored) {
                }

                try {
                    action.setY(Integer.parseInt(yField.getText().trim()));
                } catch (Exception ignored) {
                }

                try {
                    action.setWidth(Integer.parseInt(widthField.getText().trim()));
                } catch (Exception ignored) {
                }

                try {
                    action.setHeight(Integer.parseInt(heightField.getText().trim()));
                } catch (Exception ignored) {
                }

                try {
                    action.setScrollAmount(Integer.parseInt(scrollField.getText().trim()));
                } catch (Exception ignored) {
                    action.setScrollAmount(-1);
                }

                action.setKeyName(keyField.getText().trim());

                try {
                    action.setDelayMs(Integer.parseInt(delayField.getText().trim()));
                } catch (Exception ignored) {
                    action.setDelayMs(300);
                }

                action.setTemplatePath(templatePathField.getText().trim());
                action.setThreshold(thresholdSlider.getValue());

                return action;
            }
            return null;
        });
    }

    private Label createStyledLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px; -fx-font-weight: bold;");
        return lbl;
    }

    private void updatePreview(String path) {
        if (path == null || path.isBlank()) {
            templatePreviewView.setImage(null);
            templateDimLabel.setText(I18n.get("lbl.no_template"));
            return;
        }
        File f = new File(path.trim());
        if (!f.exists() || !f.isFile()) {
            templatePreviewView.setImage(null);
            templateDimLabel.setText("File not found");
            return;
        }
        try (FileInputStream in = new FileInputStream(f)) {
            Image img = new Image(in);
            templatePreviewView.setImage(img);
            templateDimLabel.setText(String.format("%.0fx%.0f px", img.getWidth(), img.getHeight()));
        } catch (Exception e) {
            templatePreviewView.setImage(null);
            templateDimLabel.setText("Invalid image");
        }
    }
}
