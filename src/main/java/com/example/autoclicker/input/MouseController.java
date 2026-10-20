package com.example.autoclicker.input;

import com.example.autoclicker.util.LoggerUtil;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

/**
 * Controller for mouse automation.
 * Uses WindowsNativeInput (Win32 API) on Windows with automatic fallback to java.awt.Robot.
 * Introduces a hold delay (50ms) between press and release so DirectX 3D games (like FC Online)
 * can register the click in their frame polling loops.
 */
public class MouseController {

    private static final int DEFAULT_HOLD_DELAY_MS = 50;
    private final Robot robot;

    public MouseController() {
        try {
            this.robot = new Robot();
            this.robot.setAutoWaitForIdle(false);
        } catch (AWTException e) {
            throw new IllegalStateException("Failed to initialize Robot in MouseController", e);
        }
    }

    public void move(int x, int y) {
        if (WindowsNativeInput.isAvailable()) {
            WindowsNativeInput.move(x, y);
        }
        robot.mouseMove(x, y);
    }

    public void smoothMove(int x, int y) {
        smoothMove(x, y, 40);
    }

    public void smoothMove(int x, int y, int durationMs) {
        if (WindowsNativeInput.isAvailable()) {
            WindowsNativeInput.smoothMove(x, y, durationMs);
        }
        robot.mouseMove(x, y);
    }

    public void scroll(int amount) {
        if (WindowsNativeInput.isAvailable()) {
            WindowsNativeInput.scroll(amount);
        } else {
            robot.mouseWheel(-amount);
        }
    }

    public void click(int x, int y) {
        click(x, y, DEFAULT_HOLD_DELAY_MS);
    }

    public void click(int x, int y, int holdMs) {
        if (WindowsNativeInput.isAvailable()) {
            WindowsNativeInput.click(x, y, holdMs);
            return;
        }
        smoothMove(x, y, 40);
        sleep(25);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        sleep(holdMs > 0 ? holdMs : DEFAULT_HOLD_DELAY_MS);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void doubleClick(int x, int y, int intervalMs) {
        doubleClick(x, y, DEFAULT_HOLD_DELAY_MS, intervalMs);
    }

    public void doubleClick(int x, int y, int holdMs, int intervalMs) {
        if (WindowsNativeInput.isAvailable()) {
            WindowsNativeInput.doubleClick(x, y, holdMs, intervalMs);
            return;
        }
        click(x, y, holdMs);
        sleep(intervalMs > 0 ? intervalMs : 50);
        click(x, y, holdMs);
    }

    public void rightClick(int x, int y) {
        rightClick(x, y, DEFAULT_HOLD_DELAY_MS);
    }

    public void rightClick(int x, int y, int holdMs) {
        if (WindowsNativeInput.isAvailable()) {
            WindowsNativeInput.rightClick(x, y, holdMs);
            return;
        }
        smoothMove(x, y, 40);
        sleep(25);
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        sleep(holdMs > 0 ? holdMs : DEFAULT_HOLD_DELAY_MS);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }

    private void sleep(int ms) {
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}


