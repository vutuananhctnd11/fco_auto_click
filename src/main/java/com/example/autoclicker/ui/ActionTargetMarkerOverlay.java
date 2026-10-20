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
 * of the currently selected action (crosshair only, no coordinate text).
 */
public class ActionTargetMarkerOverlay {

    private static Stage markerStage;

    public static synchronized void show(int screenX, int screenY) {
        show(screenX, screenY, null);
    }

    public static synchronized void show(int screenX, int screenY, String ignoredInfo) {
        Platform.runLater(() -> {
            try {
                if (markerStage == null) {
                    markerStage = new Stage();
                    markerStage.initStyle(StageStyle.TRANSPARENT);
                    markerStage.setAlwaysOnTop(true);

                    // Outer pulse ring
                    Circle outerRing = new Circle(14);
                    outerRing.setFill(Color.TRANSPARENT);
                    outerRing.setStroke(Color.web("#f43f5e", 0.9));
                    outerRing.setStrokeWidth(2.5);

                    // Inner bright dot
                    Circle innerDot = new Circle(4);
                    innerDot.setFill(Color.web("#f43f5e"));

                    StackPane crosshair = new StackPane(outerRing, innerDot);
                    crosshair.setStyle("-fx-background-color: transparent;");

                    Scene scene = new Scene(crosshair, 32, 32);
                    scene.setFill(Color.TRANSPARENT);
                    markerStage.setScene(scene);
                }

                markerStage.show();

                // Center the 32x32 crosshair exactly at screenX, screenY
                markerStage.setX(screenX - 16);
                markerStage.setY(screenY - 16);

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
