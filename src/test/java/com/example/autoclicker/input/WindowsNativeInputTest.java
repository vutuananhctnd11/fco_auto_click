package com.example.autoclicker.input;

import com.sun.jna.Platform;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WindowsNativeInputTest {

    @Test
    void testAvailabilityOnWindows() {
        if (Platform.isWindows()) {
            assertEquals(true, WindowsNativeInput.isAvailable());
        }
    }

    @Test
    void testMouseControllerExecution() {
        MouseController controller = new MouseController();
        assertDoesNotThrow(() -> controller.move(10, 10));
        assertDoesNotThrow(() -> controller.smoothMove(20, 20, 10));
        assertDoesNotThrow(() -> controller.scroll(1));
        assertDoesNotThrow(() -> controller.click(10, 10));
        com.example.autoclicker.window.WindowManager wm = new com.example.autoclicker.window.WindowManager();
        assertDoesNotThrow(() -> wm.activateWindow(new com.sun.jna.platform.win32.WinDef.HWND(com.sun.jna.Pointer.createConstant(0))));
    }

    @Test
    void testKeyboardControllerExecution() {
        KeyboardController keyboard = new KeyboardController();
        assertDoesNotThrow(() -> keyboard.pressKeyByName("ESC"));
        assertDoesNotThrow(() -> keyboard.pressAndRelease(KeyEvent.VK_ENTER, 10));
    }
}
