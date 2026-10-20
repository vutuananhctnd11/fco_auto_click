package com.example.autoclicker.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

/**
 * Windows native utility providing Per-Monitor V2 DPI awareness initialization,
 * process elevation (Administrator) check, and precise physical cursor tracking.
 */
public final class WindowsNativeUtil {

    private interface User32Extended extends Library {
        User32Extended INSTANCE = Native.load("user32", User32Extended.class);
        boolean SetProcessDpiAwarenessContext(Pointer dpiContext);
        boolean SetProcessDPIAware();
    }

    private interface Shcore extends Library {
        Shcore INSTANCE = Native.load("shcore", Shcore.class);
        int SetProcessDpiAwareness(int value);
    }

    private interface DwmApi extends Library {
        DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class);
        int DWMWA_EXTENDED_FRAME_BOUNDS = 9;
        int DwmGetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, WinDef.RECT pvAttribute, int cbAttribute);
    }

    private WindowsNativeUtil() {}

    /**
     * Gets the actual visible window bounds (excluding drop shadows and invisible resize borders) using DwmApi.
     * Returns 0 (S_OK) on success, or non-zero on failure.
     */
    public static int getExtendedFrameBounds(WinDef.HWND hwnd, WinDef.RECT rect) {
        if (!Platform.isWindows() || hwnd == null || rect == null) {
            return -1;
        }
        try {
            return DwmApi.INSTANCE.DwmGetWindowAttribute(hwnd, DwmApi.DWMWA_EXTENDED_FRAME_BOUNDS, rect, rect.size());
        } catch (Throwable t) {
            return -1;
        }
    }

    /**
     * Initializes Per-Monitor V2 DPI Awareness for the running process before UI frameworks load.
     * Prevents coordinate offset, virtual scaling, and blurring across multi-monitor setups.
     */
    public static void initDpiAwareness() {
        if (!Platform.isWindows()) {
            return;
        }
        try {
            // DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2 = ((HANDLE)-4)
            boolean success = User32Extended.INSTANCE.SetProcessDpiAwarenessContext(new Pointer(-4));
            if (success) {
                LoggerUtil.info("Windows Per-Monitor V2 DPI Awareness initialized successfully.");
                return;
            }
        } catch (Throwable ignored) {
        }

        try {
            // Fallback: SetProcessDpiAwareness(2 = PROCESS_PER_MONITOR_DPI_AWARE)
            int hr = Shcore.INSTANCE.SetProcessDpiAwareness(2);
            if (hr == 0) {
                LoggerUtil.info("Windows Per-Monitor DPI Awareness (Shcore) initialized successfully.");
                return;
            }
        } catch (Throwable ignored) {
        }

        try {
            // Fallback: SetProcessDPIAware()
            User32Extended.INSTANCE.SetProcessDPIAware();
            LoggerUtil.info("Windows System DPI Awareness initialized.");
        } catch (Throwable ignored) {
        }
    }

    /**
     * Checks if current process is running with elevated Administrator privileges.
     * Crucial for sending inputs to DirectX games protected by UIPI (like FC Online).
     */
    public static boolean isProcessElevated() {
        if (!Platform.isWindows()) {
            return true;
        }
        try {
            return Advapi32Util.isCurrentProcessElevated();
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Retrieves the absolute physical virtual desktop coordinates of the mouse cursor.
     */
    public static WinDef.POINT getCursorPos() {
        WinDef.POINT pt = new WinDef.POINT();
        if (Platform.isWindows()) {
            try {
                User32.INSTANCE.GetCursorPos(pt);
            } catch (Throwable ignored) {
            }
        }
        return pt;
    }

    /**
     * Retrieves the physical pixel bounds of the monitor containing the given physical coordinates.
     */
    public static java.awt.Rectangle getMonitorPhysicalBounds(int physicalX, int physicalY) {
        if (!Platform.isWindows()) {
            return null;
        }
        try {
            WinDef.POINT.ByValue pt = new WinDef.POINT.ByValue(physicalX, physicalY);
            com.sun.jna.platform.win32.WinUser.HMONITOR hMon = User32.INSTANCE.MonitorFromPoint(
                    pt, com.sun.jna.platform.win32.WinUser.MONITOR_DEFAULTTONEAREST);
            if (hMon != null) {
                com.sun.jna.platform.win32.WinUser.MONITORINFO info = new com.sun.jna.platform.win32.WinUser.MONITORINFO();
                if (User32.INSTANCE.GetMonitorInfo(hMon, info).booleanValue()) {
                    int x = info.rcMonitor.left;
                    int y = info.rcMonitor.top;
                    int w = info.rcMonitor.right - info.rcMonitor.left;
                    int h = info.rcMonitor.bottom - info.rcMonitor.top;
                    return new java.awt.Rectangle(x, y, w, h);
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
