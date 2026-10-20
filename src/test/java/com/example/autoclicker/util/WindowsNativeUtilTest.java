package com.example.autoclicker.util;

import com.sun.jna.platform.win32.WinDef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class WindowsNativeUtilTest {

    @Test
    void testWindowsNativeUtil() throws Exception {
        WindowsNativeUtil.initDpiAwareness();
        boolean elevated = WindowsNativeUtil.isProcessElevated();
        System.out.println("Is elevated: " + elevated);
        WinDef.POINT pt = WindowsNativeUtil.getCursorPos();
        assertNotNull(pt);
        System.out.println("Cursor pos: (" + pt.x + ", " + pt.y + ")");

        java.awt.Rectangle mon = WindowsNativeUtil.getMonitorPhysicalBounds(pt.x, pt.y);
        assertNotNull(mon);
        System.out.println("Monitor bounds: " + mon);

        java.awt.Robot robot = new java.awt.Robot();
        java.awt.image.BufferedImage shot = robot.createScreenCapture(new java.awt.Rectangle(100, 100, 102, 58));
        System.out.println("Robot capture size: " + shot.getWidth() + "x" + shot.getHeight() + ", image type: " + shot.getType());
        assertNotNull(shot);
    }
}
