package com.example.autoclicker.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Transparent floating target marker overlay displayed at the exact on-screen coordinates
 * of the currently selected action.
 */
public class ActionTargetMarkerOverlay {

    private static Stage markerStage;
    private static Label infoLabel;

    public static synchronized void show(int screenX, int screenY, String actionInfo) {
        Platform.runLater(() -> {
            try {
                if (markerStage == null) {
                    markerStage = new Stage();
                    markerStage.initStyle(StageStyle.TRANSPARENT);
                    markerStage.setAlwaysOnTop(true);

                    // Outer pulse ring
                    Circle outerRing = new Circle(14);
                    outerRing.setFill(Color.TRANSPARENT);
                    outerRing.setStroke(Color.web("#f43f5e", 0.8));
                    outerRing.setStrokeWidth(2.5);

                    // Inner bright dot
                    Circle innerDot = new Circle(4);
                    innerDot.setFill(Color.web("#f43f5e"));

                    StackPane crosshair = new StackPane(outerRing, innerDot);

                    infoLabel = new Label();
                    infoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; " +
                            "-fx-background-color: rgba(15, 23, 42, 0.9); -fx-padding: 3px 8px; " +
                            "-fx-background-radius: 4px; -fx-border-color: #f43f5e; -fx-border-radius: 4px;");

                    VBox root = new VBox(4, crosshair, infoLabel);
                    root.setAlignment(Pos.CENTER);
                    root.setStyle("-fx-background-color: transparent;");

                    Scene scene = new Scene(root);
                    scene.setFill(Color.TRANSPARENT);
                    markerStage.setScene(scene);
                }

                infoLabel.setText(actionInfo);
                markerStage.show();

                // Center the crosshair at screenX, screenY
                // The crosshair circle center is at offset X: ~45px, Y: 14px
                markerStage.setX(screenX - 45);
                markerStage.setY(screenY - 14);

            } catch (Exception ignored) {
            }
        });
    }

    public static synchronized void hide() {
        Platform.runLater(() -> {
            if (markerStage != null && markerStage.isShowing()) {
                markerStage.hide();
            }
        });
    }
}
