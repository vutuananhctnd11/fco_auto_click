package com.example.autoclicker;

import com.example.autoclicker.hotkey.GlobalHotkeyManager;
import com.example.autoclicker.hotkey.WindowsGlobalHotkeyManager;
import com.example.autoclicker.ui.MainController;
import com.example.autoclicker.util.I18n;
import com.example.autoclicker.util.LoggerUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

/**
 * Main application launcher for FCOAutoClick.
 * Developed by TunEngDev2003.
 */
public class Main extends Application {

    private GlobalHotkeyManager hotkeyManager;
    private MainController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"), I18n.getBundle());
        Parent root = loader.load();
        controller = loader.getController();

        Scene scene = new Scene(root, 1140, 820);
        primaryStage.setTitle("FCOAutoClick");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(980);
        primaryStage.setMinHeight(700);

        // Set application icon
        try (InputStream iconStream = getClass().getResourceAsStream("/icons/FCOAutoClick_logo.png")) {
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            LoggerUtil.warn("Could not load application icon: " + e.getMessage());
        }

        // Setup Windows Global Hotkeys (F8 = START, F9 = STOP)
        try {
            hotkeyManager = new WindowsGlobalHotkeyManager();
            hotkeyManager.registerHotkeys(
                    () -> Platform.runLater(() -> {
                        LoggerUtil.info("Hotkey F8 pressed: Starting automation...");
                        controller.handleStart();
                    }),
                    () -> Platform.runLater(() -> {
                        LoggerUtil.info("Hotkey F9 pressed: Stopping automation...");
                        controller.handleStop();
                    })
            );
        } catch (Throwable t) {
            LoggerUtil.warn("Could not register global hotkeys: " + t.getMessage());
        }

        primaryStage.setOnCloseRequest(event -> {
            LoggerUtil.info("Shutting down FCOAutoClick [TunEngDev2003]...");
            if (hotkeyManager != null) {
                hotkeyManager.unregisterHotkeys();
            }
            if (controller != null && controller.getAutomationEngine() != null) {
                controller.getAutomationEngine().shutdown();
            }
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
