package com.example.autoclicker.automation;

import com.example.autoclicker.input.BackgroundMouseService;
import com.example.autoclicker.input.KeyboardController;
import com.example.autoclicker.input.MouseController;
import com.example.autoclicker.screen.ScreenInfo;
import com.example.autoclicker.util.LoggerUtil;
import com.sun.jna.platform.win32.WinDef;

import java.awt.Rectangle;

/**
 * Executes individual automation actions using MouseController, BackgroundMouseService, and KeyboardController.
 * Resolves coordinates according to CoordinateMode (Window-relative or Screen-absolute).
 */
public class ActionExecutor {

    private final MouseController mouseController;
    private final KeyboardController keyboardController;

    public ActionExecutor() {
        this.mouseController = new MouseController();
        this.keyboardController = new KeyboardController();
    }

    public ActionExecutor(MouseController mouseController, KeyboardController keyboardController) {
        this.mouseController = mouseController;
        this.keyboardController = keyboardController;
    }

    /**
     * Executes an action with optional background click (PostMessage without mouse takeover).
     */
    public void execute(AutomationAction action, ScreenInfo targetScreen, Rectangle targetWindowBounds,
                        WinDef.HWND targetHwnd, boolean backgroundMode) throws InterruptedException {
        if (action == null) {
            return;
        }

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Action execution interrupted before start");
        }

        int globalX = action.getX();
        int globalY = action.getY();

        if (action.getCoordinateMode() == CoordinateMode.WINDOW_RELATIVE && targetWindowBounds != null) {
            globalX = targetWindowBounds.x + action.getX();
            globalY = targetWindowBounds.y + action.getY();
        } else if (targetScreen != null) {
            globalX = targetScreen.toGlobalX(action.getX());
            globalY = targetScreen.toGlobalY(action.getY());
        }

        boolean useBackground = backgroundMode && targetHwnd != null &&
                action.getCoordinateMode() == CoordinateMode.WINDOW_RELATIVE;

        try {
            switch (action.getType()) {
                case CLICK -> {
                    if (useBackground) {
                        LoggerUtil.info(String.format("BACKGROUND CLICK (PostMessage) at (%d, %d) [rel]",
                                action.getX(), action.getY()));
                        BackgroundMouseService.click(targetHwnd, action.getX(), action.getY());
                    } else {
                        LoggerUtil.info(String.format("CLICK at (%d, %d) [rel: %d, %d, mode: %s]",
                                globalX, globalY, action.getX(), action.getY(), action.getCoordinateMode()));
                        mouseController.click(globalX, globalY);
                    }
                }
                case DOUBLE_CLICK -> {
                    if (useBackground) {
                        LoggerUtil.info(String.format("BACKGROUND DOUBLE_CLICK (PostMessage) at (%d, %d) [rel]",
                                action.getX(), action.getY()));
                        BackgroundMouseService.doubleClick(targetHwnd, action.getX(), action.getY(), 60);
                    } else {
                        LoggerUtil.info(String.format("DOUBLE_CLICK at (%d, %d) [rel: %d, %d]",
                                globalX, globalY, action.getX(), action.getY()));
                        mouseController.doubleClick(globalX, globalY, 60);
                    }
                }
                case RIGHT_CLICK -> {
                    if (useBackground) {
                        LoggerUtil.info(String.format("BACKGROUND RIGHT_CLICK (PostMessage) at (%d, %d) [rel]",
                                action.getX(), action.getY()));
                        BackgroundMouseService.rightClick(targetHwnd, action.getX(), action.getY());
                    } else {
                        LoggerUtil.info(String.format("RIGHT_CLICK at (%d, %d) [rel: %d, %d]",
                                globalX, globalY, action.getX(), action.getY()));
                        mouseController.rightClick(globalX, globalY);
                    }
                }
                case MOVE -> {
                    LoggerUtil.info(String.format("MOVE to (%d, %d) [rel: %d, %d, mode: %s]",
                            globalX, globalY, action.getX(), action.getY(), action.getCoordinateMode()));
                    mouseController.smoothMove(globalX, globalY);
                }
                case SCROLL -> {
                    LoggerUtil.info(String.format("SCROLL amount=%d at (%d, %d)",
                            action.getScrollAmount(), globalX, globalY));
                    mouseController.move(globalX, globalY);
                    mouseController.scroll(action.getScrollAmount());
                }
                case KEY_PRESS -> {
                    LoggerUtil.info(String.format("KEY_PRESS Key=%s", action.getKeyName()));
                    keyboardController.pressKeyByName(action.getKeyName());
                }
                case WAIT -> {
                    LoggerUtil.info(String.format("WAIT %dms", action.getDelayMs()));
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Error executing action " + action.getType() + ": " + e.getMessage(), e);
        }

        // Post-action delay with responsive interruption
        int delay = action.getDelayMs();
        if (delay > 0) {
            sleepResponsive(delay);
        }
    }

    /**
     * Executes an action. Coordinates are resolved relative to window bounds or target screen.
     */
    public void execute(AutomationAction action, ScreenInfo targetScreen, Rectangle targetWindowBounds) throws InterruptedException {
        execute(action, targetScreen, targetWindowBounds, null, false);
    }

    /**
     * Backward-compatible overload without window bounds.
     */
    public void execute(AutomationAction action, ScreenInfo targetScreen) throws InterruptedException {
        execute(action, targetScreen, null, null, false);
    }

    /**
     * Sleeps in small chunks (e.g. 50ms) to respond quickly to interruption.
     */
    private void sleepResponsive(int totalMs) throws InterruptedException {
        int elapsed = 0;
        int step = 50;
        while (elapsed < totalMs) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Interrupted during action delay");
            }
            int sleepTime = Math.min(step, totalMs - elapsed);
            Thread.sleep(sleepTime);
            elapsed += sleepTime;
        }
    }
}
