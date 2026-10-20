package com.example.autoclicker.ui;

import com.example.autoclicker.automation.AutomationAction;
import com.example.autoclicker.automation.AutomationActionType;
import com.example.autoclicker.automation.CoordinateMode;
import com.example.autoclicker.util.I18n;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 * Modern custom ListCell for displaying AutomationAction items with dynamic i18n support.
 * Developed by TunEngDev2003.
 */
public class ActionListCell extends ListCell<AutomationAction> {

    @Override
    protected void updateItem(AutomationAction action, boolean empty) {
        super.updateItem(action, empty);

        if (empty || action == null) {
            setText(null);
            setGraphic(null);
            setStyle("-fx-background-color: transparent;");
            return;
        }

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 5px 8px; -fx-background-radius: 6px;");

        // 1. Index Badge (#1, #2, ...)
        int index = getIndex() + 1;
        Label indexBadge = new Label("#" + index);
        indexBadge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #38bdf8; " +
                "-fx-background-color: #0c1a2e; -fx-padding: 3px 7px; -fx-background-radius: 6px; " +
                "-fx-border-color: #0284c7; -fx-border-radius: 6px;");

        // 2. Action Type Badge with distinct icons and colors
        Label typeBadge = new Label();
        typeBadge.setStyle(getTypeBadgeStyle(action.getType()));
        typeBadge.setText(getTypeBadgeText(action.getType()));

        // 3. Target Position / Parameters with Target Marker Dot (●)
        HBox targetBox = new HBox(6);
        targetBox.setAlignment(Pos.CENTER_LEFT);

        if (action.getType() == AutomationActionType.CLICK ||
                action.getType() == AutomationActionType.DOUBLE_CLICK ||
                action.getType() == AutomationActionType.RIGHT_CLICK ||
                action.getType() == AutomationActionType.MOVE ||
                action.getType() == AutomationActionType.SCROLL) {

            // Glowing target marker dot
            Label targetDot = new Label("●");
            targetDot.setStyle("-fx-text-fill: #f43f5e; -fx-font-size: 14px; -fx-font-weight: bold;");

            Label coordLabel = new Label(String.format("(%d, %d)", action.getX(), action.getY()));
            coordLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f1f5f9; -fx-font-size: 13px;");

            String modeKey = action.getCoordinateMode() == CoordinateMode.WINDOW_RELATIVE
                    ? "coord.mode.short.window_relative"
                    : "coord.mode.short.screen_absolute";
            Label modePill = new Label(I18n.get(modeKey));
            modePill.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #38bdf8; " +
                    "-fx-background-color: #1e293b; -fx-padding: 2px 6px; -fx-background-radius: 4px;");

            targetBox.getChildren().addAll(targetDot, coordLabel, modePill);

            if (action.getType() == AutomationActionType.SCROLL) {
                Label scrollPill = new Label(I18n.get("cell.scroll", action.getScrollAmount()));
                scrollPill.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #c084fc; " +
                        "-fx-background-color: #2e1065; -fx-padding: 2px 6px; -fx-background-radius: 4px;");
                targetBox.getChildren().add(scrollPill);
            }

        } else if (action.getType() == AutomationActionType.CHECK_IMAGE) {
            Label targetDot = new Label("●");
            targetDot.setStyle("-fx-text-fill: #ec4899; -fx-font-size: 14px; -fx-font-weight: bold;");

            Label regionLabel = new Label(I18n.get("cell.region",
                    action.getWidth(), action.getHeight(), action.getX(), action.getY()));
            regionLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12px;");

            Label threshPill = new Label(I18n.get("cell.threshold", (int)(action.getThreshold() * 100)));
            threshPill.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #34d399; " +
                    "-fx-background-color: #064e3b; -fx-padding: 2px 6px; -fx-background-radius: 4px;");

            String tplName = action.getTemplatePath();
            if (tplName != null && !tplName.isBlank()) {
                int lastSlash = Math.max(tplName.lastIndexOf('/'), tplName.lastIndexOf('\\'));
                if (lastSlash >= 0) {
                    tplName = tplName.substring(lastSlash + 1);
                }
            } else {
                tplName = "--";
            }
            Label tplLabel = new Label(I18n.get("cell.template", tplName));
            tplLabel.setStyle("-fx-text-fill: #f472b6; -fx-font-size: 11px; -fx-font-weight: bold;");

            targetBox.getChildren().addAll(targetDot, regionLabel, threshPill, tplLabel);

        } else if (action.getType() == AutomationActionType.KEY_PRESS) {
            Label keyBadge = new Label(I18n.get("cell.key", action.getKeyName()));
            keyBadge.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #fbbf24; " +
                    "-fx-background-color: #451a03; -fx-padding: 2px 8px; -fx-background-radius: 4px;");
            targetBox.getChildren().add(keyBadge);

        } else if (action.getType() == AutomationActionType.WAIT) {
            Label waitBadge = new Label(I18n.get("cell.wait", action.getDelayMs()));
            waitBadge.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #cbd5e1; " +
                    "-fx-background-color: #1e293b; -fx-padding: 2px 8px; -fx-background-radius: 4px;");
            targetBox.getChildren().add(waitBadge);
        } else if (action.getType() == AutomationActionType.STOP) {
            Label stopBadge = new Label(I18n.get("cell.stop"));
            stopBadge.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #fca5a5; " +
                    "-fx-background-color: #450a0a; -fx-padding: 2px 8px; -fx-background-radius: 4px;");
            targetBox.getChildren().add(stopBadge);
        }

        // 4. Spacer
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 5. Post Delay Pill (for actions other than WAIT and STOP)
        Label delayPill = null;
        if (action.getType() != AutomationActionType.WAIT && action.getType() != AutomationActionType.STOP && action.getDelayMs() > 0) {
            delayPill = new Label(I18n.get("cell.delay", action.getDelayMs()));
            delayPill.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; " +
                    "-fx-background-color: #1e293b; -fx-padding: 3px 8px; -fx-background-radius: 4px;");
        }

        row.getChildren().addAll(indexBadge, typeBadge, targetBox, spacer);
        if (delayPill != null) {
            row.getChildren().add(delayPill);
        }

        setText(null);
        setGraphic(row);
    }

    private String getTypeBadgeText(AutomationActionType type) {
        if (type == null) return I18n.get("badge.action.click");
        return switch (type) {
            case CLICK -> I18n.get("badge.action.click");
            case DOUBLE_CLICK -> I18n.get("badge.action.double_click");
            case RIGHT_CLICK -> I18n.get("badge.action.right_click");
            case MOVE -> I18n.get("badge.action.move");
            case SCROLL -> I18n.get("badge.action.scroll");
            case CHECK_IMAGE -> I18n.get("badge.action.check_image");
            case KEY_PRESS -> I18n.get("badge.action.key_press");
            case WAIT -> I18n.get("badge.action.wait");
            case STOP -> I18n.get("badge.action.stop");
        };
    }

    private String getTypeBadgeStyle(AutomationActionType type) {
        String base = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 6px; ";
        if (type == null) return base;
        return switch (type) {
            case CLICK -> base + "-fx-text-fill: #10b981; -fx-background-color: #064e3b; -fx-border-color: #059669; -fx-border-radius: 6px;";
            case DOUBLE_CLICK -> base + "-fx-text-fill: #06b6d4; -fx-background-color: #083344; -fx-border-color: #0891b2; -fx-border-radius: 6px;";
            case RIGHT_CLICK -> base + "-fx-text-fill: #c084fc; -fx-background-color: #3b0764; -fx-border-color: #7e22ce; -fx-border-radius: 6px;";
            case MOVE -> base + "-fx-text-fill: #38bdf8; -fx-background-color: #0c4a6e; -fx-border-color: #0284c7; -fx-border-radius: 6px;";
            case SCROLL -> base + "-fx-text-fill: #a78bfa; -fx-background-color: #2e1065; -fx-border-color: #7c3aed; -fx-border-radius: 6px;";
            case CHECK_IMAGE -> base + "-fx-text-fill: #f43f5e; -fx-background-color: #4c0519; -fx-border-color: #be123c; -fx-border-radius: 6px;";
            case KEY_PRESS -> base + "-fx-text-fill: #f59e0b; -fx-background-color: #451a03; -fx-border-color: #d97706; -fx-border-radius: 6px;";
            case WAIT -> base + "-fx-text-fill: #cbd5e1; -fx-background-color: #334155; -fx-border-color: #475569; -fx-border-radius: 6px;";
            case STOP -> base + "-fx-text-fill: #ffffff; -fx-background-color: #dc2626; -fx-border-color: #ef4444; -fx-border-radius: 6px;";
            default -> base + "-fx-text-fill: #ffffff; -fx-background-color: #1e293b;";
        };
    }
}
