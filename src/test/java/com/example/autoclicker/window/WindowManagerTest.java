package com.example.autoclicker.window;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WindowManagerTest {

    @Test
    void testGetOpenWindows() {
        WindowManager windowManager = new WindowManager();
        List<WindowInfo> windows = windowManager.getOpenWindows();

        assertNotNull(windows);
        // On any running Windows Desktop, there is at least one visible window or shell window
        for (WindowInfo w : windows) {
            assertNotNull(w.getTitle());
            assertFalse(w.getTitle().isBlank());
            assertNotNull(w.getBounds());
            assertTrue(w.getWidth() > 0);
            assertTrue(w.getHeight() > 0);
        }
    }
}
