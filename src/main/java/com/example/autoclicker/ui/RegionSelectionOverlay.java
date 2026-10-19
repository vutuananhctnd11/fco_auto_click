package com.example.autoclicker.ui;

import com.example.autoclicker.automation.CoordinateMode;
import com.example.autoclicker.screen.CaptureRegion;
import com.example.autoclicker.screen.ScreenInfo;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Consumer;

/**
 * Transparent fullscreen overlay allowing the user to click and drag to select a capture region.
 * Supports Window-relative and Screen-relative coordinate translations.
 */
public class RegionSelectionOverlay {

    public static void show(ScreenInfo screen, java.awt.Rectangle windowBounds, CoordinateMode mode, Consumer<CaptureRegion> onSelected) {
        if (screen == null) {
            return;
        }

        Platform.runLater(() -> {
            Stage overlayStage = new Stage(StageStyle.TRANSPARENT);
            overlayStage.setAlwaysOnTop(true);

            // Position stage over the chosen monitor
            int stageX = screen.getX();
            int stageY = screen.getY();
            int stageW = screen.getWidth();
            int stageH = screen.getHeight();

            overlayStage.setX(stageX);
            overlayStage.setY(stageY);
            overlayStage.setWidth(stageW);
            overlayStage.setHeight(stageH);

            Pane root = new Pane();
            root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.40); -fx-cursor: crosshair;");

            String modeText = (mode == CoordinateMode.WINDOW_RELATIVE && windowBounds != null)
                    ? "WINDOW_RELATIVE (" + windowBounds.width + "x" + windowBounds.height + ")"
                    : "ABSOLUTE_SCREEN (" + screen.getName() + ")";

            // Instruction prompt in center
            Label prompt = new Label(com.example.autoclicker.util.I18n.format("overlay.region_prompt", modeText));
            prompt.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: rgba(15, 23, 42, 0.9); -fx-padding: 8px 18px; -fx-background-radius: 6px; -fx-border-color: #38bdf8; -fx-border-radius: 6px;");
            VBox promptBox = new VBox(prompt);
            promptBox.setAlignment(Pos.CENTER);
            promptBox.setLayoutX((stageW - 550) / 2.0);
            promptBox.setLayoutY(40);
            root.getChildren().add(promptBox);

            // Selection rectangle
            Rectangle rect = new Rectangle(0, 0, 0, 0);
            rect.setFill(Color.rgb(0, 162, 255, 0.25));
            rect.setStroke(Color.rgb(0, 220, 255, 0.95));
            rect.setStrokeWidth(2);
            rect.setVisible(false);
            root.getChildren().add(rect);

            final double[] startPoint = new double[2];

            root.setOnMousePressed(e -> {
                startPoint[0] = e.getX();
                startPoint[1] = e.getY();
                rect.setX(e.getX());
                rect.setY(e.getY());
                rect.setWidth(0);
                rect.setHeight(0);
                rect.setVisible(true);
            });

            root.setOnMouseDragged(e -> {
                double currentX = e.getX();
                double currentY = e.getY();

                double minX = Math.min(startPoint[0], currentX);
                double minY = Math.min(startPoint[1], currentY);
                double width = Math.abs(currentX - startPoint[0]);
                double height = Math.abs(currentY - startPoint[1]);

                rect.setX(minX);
                rect.setY(minY);
                rect.setWidth(width);
                rect.setHeight(height);
            });

            root.setOnMouseReleased(e -> {
                int screenRelX = (int) Math.round(rect.getX());
                int screenRelY = (int) Math.round(rect.getY());
                int rw = (int) Math.round(rect.getWidth());
                int rh = (int) Math.round(rect.getHeight());

                int globalX = stageX + screenRelX;
                int globalY = stageY + screenRelY;

                int finalX = screenRelX;
                int finalY = screenRelY;

                if (mode == CoordinateMode.WINDOW_RELATIVE && windowBounds != null) {
                    finalX = globalX - windowBounds.x;
                    finalY = globalY - windowBounds.y;
                }

                overlayStage.close();

                if (rw > 5 && rh > 5) {
                    onSelected.accept(new CaptureRegion(finalX, finalY, rw, rh, mode));
                }
            });

            Scene scene = new Scene(root, stageW, stageH);
            scene.setFill(Color.TRANSPARENT);
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    overlayStage.close();
                }
            });

            overlayStage.setScene(scene);
            overlayStage.show();
        });
    }

    public static void show(ScreenInfo screen, Consumer<CaptureRegion> onSelected) {
        show(screen, null, CoordinateMode.ABSOLUTE_SCREEN, onSelected);
    }
}
