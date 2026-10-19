package com.example.autoclicker.ui;

import com.example.autoclicker.automation.CoordinateMode;
import com.example.autoclicker.screen.ScreenInfo;
import com.example.autoclicker.util.LoggerUtil;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.function.Consumer;

/**
 * Fullscreen transparent overlay to capture a single mouse click coordinate without triggering native click actions.
 * Automatically translates to Window-relative or Screen-relative coordinates.
 */
public class CoordinatePickerOverlay {

    public static void pick(ScreenInfo screen, Rectangle windowBounds, CoordinateMode mode, Consumer<Point> onPicked) {
        Platform.runLater(() -> {
            Stage overlayStage = new Stage(StageStyle.TRANSPARENT);
            overlayStage.setAlwaysOnTop(true);

            // Determine overlay placement
            int stageX = screen != null ? screen.getX() : 0;
            int stageY = screen != null ? screen.getY() : 0;
            int stageW = screen != null ? screen.getWidth() : 1920;
            int stageH = screen != null ? screen.getHeight() : 1080;

            overlayStage.setX(stageX);
            overlayStage.setY(stageY);
            overlayStage.setWidth(stageW);
            overlayStage.setHeight(stageH);

            Pane root = new Pane();
            root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.25); -fx-cursor: crosshair;");

            // Prompt HUD at top
            String modeText = (mode == CoordinateMode.WINDOW_RELATIVE && windowBounds != null)
                    ? "WINDOW_RELATIVE (" + windowBounds.width + "x" + windowBounds.height + ")"
                    : "ABSOLUTE_SCREEN";

            Label prompt = new Label(com.example.autoclicker.util.I18n.format("overlay.coord_prompt", modeText));
            prompt.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: rgba(15, 23, 42, 0.9); -fx-padding: 8px 18px; -fx-background-radius: 6px; -fx-border-color: #38bdf8; -fx-border-radius: 6px;");

            VBox promptBox = new VBox(prompt);
            promptBox.setAlignment(Pos.CENTER);
            promptBox.setLayoutX((stageW - 550) / 2.0);
            promptBox.setLayoutY(30);
            root.getChildren().add(promptBox);

            // Real-time coordinate tracker badge
            Label coordBadge = new Label("(0, 0)");
            coordBadge.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 12px; -fx-background-color: rgba(0,0,0,0.75); -fx-padding: 4px 8px; -fx-background-radius: 4px;");
            coordBadge.setVisible(false);
            root.getChildren().add(coordBadge);

            // Target crosshair lines
            Line lineH = new Line(0, 0, stageW, 0);
            lineH.setStroke(Color.rgb(56, 189, 248, 0.6));
            lineH.setStrokeWidth(1);
            lineH.setVisible(false);

            Line lineV = new Line(0, 0, 0, stageH);
            lineV.setStroke(Color.rgb(56, 189, 248, 0.6));
            lineV.setStrokeWidth(1);
            lineV.setVisible(false);

            Circle circle = new Circle(0, 0, 6);
            circle.setFill(Color.TRANSPARENT);
            circle.setStroke(Color.rgb(244, 63, 94, 0.9));
            circle.setStrokeWidth(2);
            circle.setVisible(false);

            root.getChildren().addAll(lineH, lineV, circle);

            root.setOnMouseMoved(e -> {
                double mx = e.getX();
                double my = e.getY();

                lineH.setStartY(my);
                lineH.setEndY(my);
                lineV.setStartX(mx);
                lineV.setEndX(mx);
                circle.setCenterX(mx);
                circle.setCenterY(my);

                lineH.setVisible(true);
                lineV.setVisible(true);
                circle.setVisible(true);

                // Compute display coords
                int globalX = (int) Math.round(stageX + mx);
                int globalY = (int) Math.round(stageY + my);
                int dispX = globalX;
                int dispY = globalY;

                if (mode == CoordinateMode.WINDOW_RELATIVE && windowBounds != null) {
                    dispX = globalX - windowBounds.x;
                    dispY = globalY - windowBounds.y;
                } else if (screen != null) {
                    dispX = globalX - screen.getX();
                    dispY = globalY - screen.getY();
                }

                coordBadge.setText(String.format("X: %d, Y: %d", dispX, dispY));
                coordBadge.setLayoutX(mx + 15);
                coordBadge.setLayoutY(my + 15);
                coordBadge.setVisible(true);
            });

            root.setOnMouseClicked(e -> {
                int globalX = (int) Math.round(stageX + e.getX());
                int globalY = (int) Math.round(stageY + e.getY());

                int finalX = globalX;
                int finalY = globalY;

                if (mode == CoordinateMode.WINDOW_RELATIVE && windowBounds != null) {
                    finalX = globalX - windowBounds.x;
                    finalY = globalY - windowBounds.y;
                } else if (screen != null) {
                    finalX = globalX - screen.getX();
                    finalY = globalY - screen.getY();
                }

                overlayStage.close();
                LoggerUtil.info(String.format("Coordinate picked: (%d, %d) [global: %d, %d, mode: %s]",
                        finalX, finalY, globalX, globalY, mode));

                if (onPicked != null) {
                    onPicked.accept(new Point(finalX, finalY));
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
}
