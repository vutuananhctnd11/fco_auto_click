package com.example.autoclicker.ui;

import com.example.autoclicker.automation.CoordinateMode;
import com.example.autoclicker.screen.ScreenInfo;
import com.example.autoclicker.util.I18n;
import com.example.autoclicker.util.LoggerUtil;
import com.example.autoclicker.util.WindowsNativeUtil;
import com.sun.jna.platform.win32.WinDef;
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
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Multi-screen transparent overlay to capture mouse click coordinates with physical Win32 precision.
 * Covers all connected monitors so dual-monitor setups never suffer from coordinate drift,
 * scaling mismatch, or unclickable target areas.
 */
public class CoordinatePickerOverlay {

    public static void pick(ScreenInfo targetScreen, Rectangle windowBounds, CoordinateMode mode, Consumer<Point> onPicked) {
        Platform.runLater(() -> {
            List<Screen> screens = Screen.getScreens();
            List<Stage> overlayStages = new ArrayList<>();
            AtomicBoolean picked = new AtomicBoolean(false);

            Runnable closeAll = () -> {
                for (Stage st : overlayStages) {
                    try {
                        st.close();
                    } catch (Exception ignored) {
                    }
                }
                overlayStages.clear();
            };

            Consumer<WinDef.POINT> finishSelection = pt -> {
                if (picked.compareAndSet(false, true)) {
                    int finalX = pt.x;
                    int finalY = pt.y;

                    if (mode == CoordinateMode.WINDOW_RELATIVE && windowBounds != null) {
                        finalX = pt.x - windowBounds.x;
                        finalY = pt.y - windowBounds.y;
                    }

                    closeAll.run();
                    LoggerUtil.info(String.format("Coordinate picked: (%d, %d) [Physical cursor: (%d, %d), Window: (%d, %d), Mode: %s]",
                            finalX, finalY, pt.x, pt.y,
                            windowBounds != null ? windowBounds.x : 0,
                            windowBounds != null ? windowBounds.y : 0,
                            mode));

                    if (onPicked != null) {
                        onPicked.accept(new Point(finalX, finalY));
                    }
                }
            };

            String modeText = (mode == CoordinateMode.WINDOW_RELATIVE && windowBounds != null)
                    ? "WINDOW_RELATIVE (" + windowBounds.width + "x" + windowBounds.height + " at " + windowBounds.x + "," + windowBounds.y + ")"
                    : "ABSOLUTE_SCREEN";

            for (Screen fxScreen : screens) {
                Stage stage = new Stage(StageStyle.TRANSPARENT);
                stage.setAlwaysOnTop(true);

                double stageX = fxScreen.getBounds().getMinX();
                double stageY = fxScreen.getBounds().getMinY();
                double stageW = fxScreen.getBounds().getWidth();
                double stageH = fxScreen.getBounds().getHeight();

                stage.setX(stageX);
                stage.setY(stageY);
                stage.setWidth(stageW);
                stage.setHeight(stageH);

                Pane root = new Pane();
                root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.25); -fx-cursor: crosshair;");

                // HUD Prompt at top of each screen
                Label prompt = new Label(I18n.format("overlay.coord_prompt", modeText) +
                        " \u2022 [Click / SPACE / ENTER: Chọn] [ESC: Hủy]");
                prompt.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: rgba(15, 23, 42, 0.92); -fx-padding: 8px 18px; -fx-background-radius: 6px; -fx-border-color: #38bdf8; -fx-border-radius: 6px;");

                VBox promptBox = new VBox(prompt);
                promptBox.setAlignment(Pos.CENTER);
                promptBox.setLayoutX(Math.max(20, (stageW - 650) / 2.0));
                promptBox.setLayoutY(30);
                root.getChildren().add(promptBox);

                // Coordinate tracker badge
                Label coordBadge = new Label("(0, 0)");
                coordBadge.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 12px; -fx-background-color: rgba(0,0,0,0.85); -fx-padding: 4px 10px; -fx-background-radius: 4px; -fx-border-color: #38bdf8; -fx-border-width: 1px; -fx-border-radius: 4px;");
                coordBadge.setVisible(false);
                root.getChildren().add(coordBadge);

                // Crosshair lines
                Line lineH = new Line(0, 0, stageW, 0);
                lineH.setStroke(Color.rgb(56, 189, 248, 0.7));
                lineH.setStrokeWidth(1);
                lineH.setVisible(false);

                Line lineV = new Line(0, 0, 0, stageH);
                lineV.setStroke(Color.rgb(56, 189, 248, 0.7));
                lineV.setStrokeWidth(1);
                lineV.setVisible(false);

                Circle circle = new Circle(0, 0, 7);
                circle.setFill(Color.TRANSPARENT);
                circle.setStroke(Color.rgb(244, 63, 94, 0.95));
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

                    // Fetch accurate Win32 cursor position
                    WinDef.POINT pt = WindowsNativeUtil.getCursorPos();
                    int dispX = pt.x;
                    int dispY = pt.y;

                    if (mode == CoordinateMode.WINDOW_RELATIVE && windowBounds != null) {
                        dispX = pt.x - windowBounds.x;
                        dispY = pt.y - windowBounds.y;
                        coordBadge.setText(String.format("Window Rel: X=%d, Y=%d  (Global: %d, %d)", dispX, dispY, pt.x, pt.y));
                    } else {
                        coordBadge.setText(String.format("Global: X=%d, Y=%d", dispX, dispY));
                    }

                    // Keep badge within screen boundary
                    double badgeX = mx + 16;
                    double badgeY = my + 16;
                    if (badgeX + 220 > stageW) badgeX = mx - 230;
                    if (badgeY + 40 > stageH) badgeY = my - 35;

                    coordBadge.setLayoutX(Math.max(10, badgeX));
                    coordBadge.setLayoutY(Math.max(10, badgeY));
                    coordBadge.setVisible(true);
                });

                root.setOnMouseClicked(e -> {
                    WinDef.POINT pt = WindowsNativeUtil.getCursorPos();
                    finishSelection.accept(pt);
                });

                Scene scene = new Scene(root, stageW, stageH);
                scene.setFill(Color.TRANSPARENT);

                scene.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.ESCAPE) {
                        closeAll.run();
                    } else if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) {
                        WinDef.POINT pt = WindowsNativeUtil.getCursorPos();
                        finishSelection.accept(pt);
                    }
                });

                stage.setScene(scene);
                overlayStages.add(stage);
            }

            for (Stage st : overlayStages) {
                st.show();
            }
        });
    }
}
