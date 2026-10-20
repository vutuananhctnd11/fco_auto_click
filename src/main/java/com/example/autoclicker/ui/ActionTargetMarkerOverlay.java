package com.example.autoclicker.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Transparent floating target marker overlay displayed at the exact on-screen coordinates
 * of the currently selected or edited action. Features a sniper crosshair and real-time
 * action badge (e.g. CLICK / MOVE coordinates).
 */
public class ActionTargetMarkerOverlay {

    private static Stage markerStage;
    private static Label infoBadge;

    public static synchronized void show(int physicalScreenX, int physicalScreenY) {
        show(physicalScreenX, physicalScreenY, null);
    }

    public static synchronized void show(int physicalScreenX, int physicalScreenY, String actionInfo) {
        Platform.runLater(() -> {
            try {
                if (markerStage == null) {
                    markerStage = new Stage();
                    markerStage.initStyle(StageStyle.TRANSPARENT);
                    markerStage.setAlwaysOnTop(true);

                    Pane root = new Pane();
                    root.setStyle("-fx-background-color: transparent;");

                    // Center crosshair container (located at local 24, 24)
                    StackPane crosshairPane = new StackPane();
                    crosshairPane.setLayoutX(8);
                    crosshairPane.setLayoutY(8);
                    crosshairPane.setPrefSize(32, 32);

                    // Outer pulse ring
                    Circle outerRing = new Circle(14);
                    outerRing.setFill(Color.TRANSPARENT);
                    outerRing.setStroke(Color.web("#f43f5e", 0.95));
                    outerRing.setStrokeWidth(2.0);

                    // Inner bright dot
                    Circle innerDot = new Circle(3.5);
                    innerDot.setFill(Color.web("#f43f5e"));

                    // Crosshair tick marks (top, bottom, left, right)
                    Line lineTop = new Line(16, 0, 16, 6);
                    lineTop.setStroke(Color.web("#f43f5e", 0.95));
                    lineTop.setStrokeWidth(2.0);

                    Line lineBottom = new Line(16, 26, 16, 32);
                    lineBottom.setStroke(Color.web("#f43f5e", 0.95));
                    lineBottom.setStrokeWidth(2.0);

                    Line lineLeft = new Line(0, 16, 6, 16);
                    lineLeft.setStroke(Color.web("#f43f5e", 0.95));
                    lineLeft.setStrokeWidth(2.0);

                    Line lineRight = new Line(26, 16, 32, 16);
                    lineRight.setStroke(Color.web("#f43f5e", 0.95));
                    lineRight.setStrokeWidth(2.0);

                    Pane ticksPane = new Pane(lineTop, lineBottom, lineLeft, lineRight);
                    ticksPane.setMouseTransparent(true);

                    crosshairPane.getChildren().addAll(outerRing, innerDot, ticksPane);

                    // Info Badge displaying action name and coordinates
                    infoBadge = new Label("");
                    infoBadge.setLayoutX(46);
                    infoBadge.setLayoutY(14);
                    infoBadge.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 11px; -fx-font-weight: bold; " +
                            "-fx-background-color: rgba(15, 23, 42, 0.92); -fx-padding: 3px 8px; " +
                            "-fx-background-radius: 4px; -fx-border-color: #f43f5e; -fx-border-width: 1px; -fx-border-radius: 4px;");

                    root.getChildren().addAll(crosshairPane, infoBadge);

                    Scene scene = new Scene(root, 280, 48);
                    scene.setFill(Color.TRANSPARENT);
                    markerStage.setScene(scene);
                }

                if (actionInfo != null && !actionInfo.isBlank()) {
                    infoBadge.setText(actionInfo);
                    infoBadge.setVisible(true);
                } else {
                    infoBadge.setVisible(false);
                }

                // Accurate translation from Win32 physical coordinates to JavaFX stage coordinates
                double targetFxX = physicalScreenX;
                double targetFxY = physicalScreenY;

                java.awt.Rectangle monBounds = com.example.autoclicker.util.WindowsNativeUtil.getMonitorPhysicalBounds(physicalScreenX, physicalScreenY);
                if (monBounds != null) {
                    Screen targetScreen = null;
                    if (monBounds.x == 0 && monBounds.y == 0) {
                        targetScreen = Screen.getPrimary();
                    } else {
                        // Match non-primary screen by relative orientation/position
                        for (Screen s : Screen.getScreens()) {
                            if (s != Screen.getPrimary()) {
                                boolean fxRight = s.getBounds().getMinX() > 0;
                                boolean monRight = monBounds.x > 0;
                                boolean fxLeft = s.getBounds().getMinX() < 0;
                                boolean monLeft = monBounds.x < 0;
                                if ((fxRight && monRight) || (fxLeft && monLeft) || (monBounds.x == 0 && s.getBounds().getMinX() == 0)) {
                                    targetScreen = s;
                                    break;
                                }
                            }
                        }
                    }
                    if (targetScreen == null) {
                        targetScreen = Screen.getPrimary();
                    }

                    double scaleX = targetScreen.getOutputScaleX();
                    double scaleY = targetScreen.getOutputScaleY();
                    double offsetPhysX = physicalScreenX - monBounds.x;
                    double offsetPhysY = physicalScreenY - monBounds.y;

                    targetFxX = targetScreen.getBounds().getMinX() + (offsetPhysX / scaleX);
                    targetFxY = targetScreen.getBounds().getMinY() + (offsetPhysY / scaleY);
                } else {
                    Screen p = Screen.getPrimary();
                    double sx = (p != null) ? p.getOutputScaleX() : 1.0;
                    double sy = (p != null) ? p.getOutputScaleY() : 1.0;
                    targetFxX = physicalScreenX / sx;
                    targetFxY = physicalScreenY / sy;
                }

                markerStage.show();
                // Center the crosshair (local center is at 24, 24) exactly at targetFxX, targetFxY
                markerStage.setX(targetFxX - 24);
                markerStage.setY(targetFxY - 24);

            } catch (Exception ignored) {
            }
        });
    }

    public static synchronized void hide() {
        if (Platform.isFxApplicationThread()) {
            if (markerStage != null && markerStage.isShowing()) {
                markerStage.hide();
            }
        } else {
            Platform.runLater(() -> {
                if (markerStage != null && markerStage.isShowing()) {
                    markerStage.hide();
                }
            });
        }
    }
}
