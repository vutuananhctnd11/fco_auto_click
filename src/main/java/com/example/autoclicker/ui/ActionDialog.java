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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;

/**
 * Modal dialog to add or edit an AutomationAction with i18n support.
 * Dynamically switches form fields between Mouse/Keyboard actions and CHECK_IMAGE action.
 */
public class ActionDialog extends Dialog<AutomationAction> {

    private final ComboBox<AutomationActionType> typeCombo;
    private final ComboBox<CoordinateMode> modeCombo;

    // Mouse fields
    private final TextField xField;
    private final TextField yField;
    private final Button pickCoordBtn;

    // Keyboard & General fields
    private final TextField keyField;
    private final TextField delayField;

    // CHECK_IMAGE specific fields
    private final TextField widthField;
    private final TextField heightField;
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
        dialogPane.setPrefWidth(520);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(14, 18, 14, 18));

        typeCombo = new ComboBox<>(FXCollections.observableArrayList(AutomationActionType.values()));
        modeCombo = new ComboBox<>(FXCollections.observableArrayList(CoordinateMode.values()));
        modeCombo.setValue(CoordinateMode.WINDOW_RELATIVE);

        xField = new TextField("0");
        yField = new TextField("0");
        widthField = new TextField("300");
        heightField = new TextField("200");
        keyField = new TextField("ESC");
        delayField = new TextField("300");

        pickCoordBtn = new Button(I18n.get("dialog.action.pick_btn"));
        pickCoordBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold;");

        pickRegionBtn = new Button(I18n.get("btn.pick_region"));
        pickRegionBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold;");

        templatePathField = new TextField("");
        templatePathField.setPromptText(I18n.get("prompt.template_path"));
        browseTemplateBtn = new Button(I18n.get("btn.browse"));

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
        testImageBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold;");
        testResultBadge = new Label(I18n.get("lbl.untested"));
        testResultBadge.setStyle("-fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        testConfidenceLabel = new Label("Confidence: --");
        testConfidenceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

        // Load existing values
        if (existingAction != null) {
            typeCombo.setValue(existingAction.getType());
            modeCombo.setValue(existingAction.getCoordinateMode() != null ? existingAction.getCoordinateMode() : CoordinateMode.WINDOW_RELATIVE);
            xField.setText(String.valueOf(existingAction.getX()));
            yField.setText(String.valueOf(existingAction.getY()));
            widthField.setText(String.valueOf(existingAction.getWidth() > 0 ? existingAction.getWidth() : 300));
            heightField.setText(String.valueOf(existingAction.getHeight() > 0 ? existingAction.getHeight() : 200));
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

                testConfidenceLabel.setText(String.format(Locale.US, "Confidence: %.2f", res.getConfidence()));
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
        Label typeLbl = new Label(I18n.get("dialog.action.type"));
        Label modeLbl = new Label(I18n.get("dialog.action.mode"));
        Label xLbl = new Label(I18n.get("dialog.action.x"));
        Label yLbl = new Label(I18n.get("dialog.action.y"));
        Label wLbl = new Label("Width:");
        Label hLbl = new Label("Height:");
        Label keyLbl = new Label(I18n.get("dialog.action.key"));
        Label delayLbl = new Label(I18n.get("dialog.action.delay"));
        Label tplLbl = new Label(I18n.get("lbl.template_image"));
        Label threshLbl = new Label(I18n.get("lbl.threshold"));

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

        grid.add(tplLbl, 0, 6);
        VBox tplBox = new VBox(6,
                new HBox(6, templatePathField, browseTemplateBtn),
                new HBox(8, templatePreviewView, templateDimLabel));
        grid.add(tplBox, 1, 6);

        grid.add(threshLbl, 0, 7);
        HBox threshBox = new HBox(8, thresholdSlider, thresholdField);
        grid.add(threshBox, 1, 7);

        Label testLbl = new Label("Test Matching:");
        HBox testBox = new HBox(8, testImageBtn, testResultBadge, testConfidenceLabel);
        grid.add(testLbl, 0, 8);
        grid.add(testBox, 1, 8);

        grid.add(keyLbl, 0, 9);
        grid.add(keyField, 1, 9);

        grid.add(delayLbl, 0, 10);
        grid.add(delayField, 1, 10);

        Label tipLbl = new Label(I18n.get("dialog.action.key_tip"));
        tipLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");
        grid.add(tipLbl, 1, 11);

        Runnable updateVisibility = () -> {
            AutomationActionType type = typeCombo.getValue();
            boolean isCheckImage = (type == AutomationActionType.CHECK_IMAGE);
            boolean isMouse = (type == AutomationActionType.CLICK || type == AutomationActionType.DOUBLE_CLICK || type == AutomationActionType.RIGHT_CLICK);
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

        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                AutomationAction action = new AutomationAction();
                action.setType(typeCombo.getValue());
                action.setCoordinateMode(modeCombo.getValue());

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

    private void updatePreview(String path) {
        if (path == null || path.isBlank()) {
            templatePreviewView.setImage(null);
            templateDimLabel.setText(I18n.get("lbl.no_template"));
            return;
        }
        File f = new File(path.trim());
        if (f.exists() && f.isFile()) {
            try {
                Image img = new Image(f.toURI().toString());
                templatePreviewView.setImage(img);
                templateDimLabel.setText(String.format("%.0fx%.0f px", img.getWidth(), img.getHeight()));
            } catch (Exception e) {
                templatePreviewView.setImage(null);
                templateDimLabel.setText("Error loading");
            }
        } else {
            templatePreviewView.setImage(null);
            templateDimLabel.setText("File not found");
        }
    }

    public ActionDialog(AutomationAction existingAction) {
        this(existingAction, null, null);
    }
}
