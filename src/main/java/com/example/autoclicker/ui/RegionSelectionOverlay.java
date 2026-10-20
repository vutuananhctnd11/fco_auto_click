package com.example.autoclicker.ui;

import com.example.autoclicker.automation.CoordinateMode;
import com.example.autoclicker.screen.CaptureRegion;
import com.example.autoclicker.screen.ScreenInfo;
import com.example.autoclicker.util.I18n;
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
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Transparent fullscreen multi-monitor overlay allowing the user to click and drag to select
 * an image capture region with Win32 physical precision.
 */
public class RegionSelectionOverlay {

    public static void show(ScreenInfo targetScreen, java.awt.Rectangle windowBounds, CoordinateMode mode, Consumer<CaptureRegion> onSelected) {
        Platform.runLater(() -> {
            List<Screen> screens = Screen.getScreens();
            List<Stage> overlayStages = new ArrayList<>();
            AtomicBoolean selected = new AtomicBoolean(false);

            Runnable closeAll = () -> {
                for (Stage st : overlayStages) {
                    try {
                        st.close();
                    } catch (Exception ignored) {
                    }
                }
                overlayStages.clear();
            };

            String modeText = (mode == CoordinateMode.WINDOW_RELATIVE && windowBounds != null)
                    ? "WINDOW_RELATIVE (" + windowBounds.width + "x" + windowBounds.height + ")"
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
                root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.35); -fx-cursor: crosshair;");

                Label prompt = new Label(I18n.format("overlay.region_prompt", modeText) + " \u2022 [Kéo thả chuột để chọn | ESC để hủy]");
                prompt.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: rgba(15, 23, 42, 0.92); -fx-padding: 8px 18px; -fx-background-radius: 6px; -fx-border-color: #38bdf8; -fx-border-radius: 6px;");
                VBox promptBox = new VBox(prompt);
                promptBox.setAlignment(Pos.CENTER);
                promptBox.setLayoutX(Math.max(20, (stageW - 650) / 2.0));
                promptBox.setLayoutY(30);
                root.getChildren().add(promptBox);

                // Selection rectangle visual element
                Rectangle rect = new Rectangle(0, 0, 0, 0);
                rect.setFill(Color.rgb(0, 162, 255, 0.25));
                rect.setStroke(Color.rgb(0, 220, 255, 0.95));
                rect.setStrokeWidth(2);
                rect.setVisible(false);
                root.getChildren().add(rect);

                final double[] startLocal = new double[2];
                final WinDef.POINT[] startPhysical = new WinDef.POINT[1];

                root.setOnMousePressed(e -> {
                    startLocal[0] = e.getX();
                    startLocal[1] = e.getY();
                    startPhysical[0] = WindowsNativeUtil.getCursorPos();

                    rect.setX(e.getX());
                    rect.setY(e.getY());
                    rect.setWidth(0);
                    rect.setHeight(0);
                    rect.setVisible(true);
                });

                root.setOnMouseDragged(e -> {
                    double currentX = e.getX();
                    double currentY = e.getY();

                    double minX = Math.min(startLocal[0], currentX);
                    double minY = Math.min(startLocal[1], currentY);
                    double width = Math.abs(currentX - startLocal[0]);
                    double height = Math.abs(currentY - startLocal[1]);

                    rect.setX(minX);
                    rect.setY(minY);
                    rect.setWidth(width);
                    rect.setHeight(height);
                });

                root.setOnMouseReleased(e -> {
                    WinDef.POINT endPhysical = WindowsNativeUtil.getCursorPos();
                    if (startPhysical[0] == null) {
                        startPhysical[0] = endPhysical;
                    }

                    int minPhysX = Math.min(startPhysical[0].x, endPhysical.x);
                    int minPhysY = Math.min(startPhysical[0].y, endPhysical.y);
                    int rw = Math.abs(endPhysical.x - startPhysical[0].x);
                    int rh = Math.abs(endPhysical.y - startPhysical[0].y);

                    int finalX = minPhysX;
                    int finalY = minPhysY;

                    if (mode == CoordinateMode.WINDOW_RELATIVE && windowBounds != null) {
                        finalX = minPhysX - windowBounds.x;
                        finalY = minPhysY - windowBounds.y;
                    }

                    if (selected.compareAndSet(false, true)) {
                        closeAll.run();
                        if (rw > 5 && rh > 5 && onSelected != null) {
                            onSelected.accept(new CaptureRegion(finalX, finalY, rw, rh, mode));
                        }
                    }
                });

                Scene scene = new Scene(root, stageW, stageH);
                scene.setFill(Color.TRANSPARENT);
                scene.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.ESCAPE) {
                        closeAll.run();
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

    public static void show(ScreenInfo screen, Consumer<CaptureRegion> onSelected) {
        show(screen, null, CoordinateMode.ABSOLUTE_SCREEN, onSelected);
    }
}
