package com.example.autoclicker.input;

import com.example.autoclicker.util.LoggerUtil;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Low-level controller for keyboard automation using java.awt.Robot.
 */
public class KeyboardController {

    private final Robot robot;
    private static final Map<String, Integer> KEY_CODE_MAP = new HashMap<>();

    static {
        // Special keys
        KEY_CODE_MAP.put("ESC", KeyEvent.VK_ESCAPE);
        KEY_CODE_MAP.put("ESCAPE", KeyEvent.VK_ESCAPE);
        KEY_CODE_MAP.put("ENTER", KeyEvent.VK_ENTER);
        KEY_CODE_MAP.put("SPACE", KeyEvent.VK_SPACE);
        KEY_CODE_MAP.put("TAB", KeyEvent.VK_TAB);
        KEY_CODE_MAP.put("BACKSPACE", KeyEvent.VK_BACK_SPACE);
        KEY_CODE_MAP.put("SHIFT", KeyEvent.VK_SHIFT);
        KEY_CODE_MAP.put("CTRL", KeyEvent.VK_CONTROL);
        KEY_CODE_MAP.put("ALT", KeyEvent.VK_ALT);
        KEY_CODE_MAP.put("UP", KeyEvent.VK_UP);
        KEY_CODE_MAP.put("DOWN", KeyEvent.VK_DOWN);
        KEY_CODE_MAP.put("LEFT", KeyEvent.VK_LEFT);
        KEY_CODE_MAP.put("RIGHT", KeyEvent.VK_RIGHT);

        // Function keys
        for (int i = 1; i <= 12; i++) {
            KEY_CODE_MAP.put("F" + i, KeyEvent.VK_F1 + (i - 1));
        }

        // Numbers 0-9
        for (int i = 0; i <= 9; i++) {
            KEY_CODE_MAP.put(String.valueOf(i), KeyEvent.VK_0 + i);
        }

        // Letters A-Z
        for (char c = 'A'; c <= 'Z'; c++) {
            KEY_CODE_MAP.put(String.valueOf(c), KeyEvent.VK_A + (c - 'A'));
        }
    }

    public KeyboardController() {
        try {
            this.robot = new Robot();
            this.robot.setAutoWaitForIdle(false);
        } catch (AWTException e) {
            throw new IllegalStateException("Failed to initialize Robot in KeyboardController", e);
        }
    }

    public void pressAndRelease(int keyCode) {
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
    }

    public void pressKeyByName(String keyName) {
        if (keyName == null || keyName.isBlank()) {
            return;
        }
        String normalized = keyName.trim().toUpperCase(Locale.ROOT);
        Integer keyCode = KEY_CODE_MAP.get(normalized);
        if (keyCode != null) {
            pressAndRelease(keyCode);
        } else if (normalized.length() == 1) {
            int code = KeyEvent.getExtendedKeyCodeForChar(normalized.charAt(0));
            if (code != KeyEvent.VK_UNDEFINED) {
                pressAndRelease(code);
            } else {
                LoggerUtil.warn("Unknown key name or character: " + keyName);
            }
        } else {
            LoggerUtil.warn("Unknown key name: " + keyName);
        }
    }
}
