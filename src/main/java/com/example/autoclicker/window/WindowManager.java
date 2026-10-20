package com.example.autoclicker.window;

import com.example.autoclicker.util.LoggerUtil;
import com.example.autoclicker.util.WindowsNativeUtil;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages detection, enumeration, activation and bounds tracking for active Windows application windows.
 */
public class WindowManager {

    private static final BaseTSD.ULONG_PTR ZERO_EXTRA_INFO = new BaseTSD.ULONG_PTR(0);
    private final User32 user32;

    public WindowManager() {
        this.user32 = User32.INSTANCE;
    }

    /**
     * Enumerates all currently visible top-level windows with non-empty titles.
     */
    public List<WindowInfo> getOpenWindows() {
        List<WindowInfo> result = new ArrayList<>();

        user32.EnumWindows((hwnd, data) -> {
            if (user32.IsWindowVisible(hwnd)) {
                char[] buffer = new char[512];
                int len = user32.GetWindowText(hwnd, buffer, 512);
                if (len > 0) {
                    String title = new String(buffer, 0, len).trim();
                    Rectangle bounds = getWindowBounds(hwnd);
                    if (bounds != null) {
                        // Filter out non-interactive micro-windows, off-screen windows (-32000), or shells
                        if (bounds.width > 50 && bounds.height > 50 && bounds.x > -30000 && bounds.y > -30000
                                && !title.equalsIgnoreCase("Program Manager")) {
                            result.add(new WindowInfo(hwnd, title, bounds));
                        }
                    }
                }
            }
            return true;
        }, null);

        result.sort(Comparator.comparing(WindowInfo::getTitle, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    /**
     * Finds the first active window matching the given title query (case-insensitive).
     */
    public WindowInfo findWindowByTitle(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String lowerQuery = query.toLowerCase();
        for (WindowInfo info : getOpenWindows()) {
            if (info.getTitle().toLowerCase().contains(lowerQuery)) {
                return info;
            }
        }
        return null;
    }

    /**
     * Retrieves up-to-date physical bounds for the given window handle.
     * Uses DWM Extended Frame Bounds to eliminate invisible drop shadows and maximized padding.
     */
    public Rectangle getWindowBounds(HWND hwnd) {
        if (hwnd == null || !user32.IsWindow(hwnd)) {
            return null;
        }

        // 1. Try DWM Extended Frame Bounds (removes invisible drop shadow & maximized window margins)
        WinDef.RECT dwmRect = new WinDef.RECT();
        int hr = WindowsNativeUtil.getExtendedFrameBounds(hwnd, dwmRect);
        if (hr == 0) {
            int w = dwmRect.right - dwmRect.left;
            int h = dwmRect.bottom - dwmRect.top;
            if (w > 0 && h > 0) {
                return new Rectangle(dwmRect.left, dwmRect.top, w, h);
            }
        }

        // 2. Fallback to standard Win32 GetWindowRect
        WinDef.RECT rect = new WinDef.RECT();
        if (user32.GetWindowRect(hwnd, rect)) {
            return new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
        }
        return null;
    }

    /**
     * Checks if the window handle still exists and is currently visible.
     */
    public boolean isWindowValid(HWND hwnd) {
        return hwnd != null && user32.IsWindow(hwnd) && user32.IsWindowVisible(hwnd);
    }

    /**
     * Restores, brings to top, and forcibly activates the window so it receives keyboard and mouse input.
     * Uses Windows ALT simulation trick to bypass SetForegroundWindow restrictions.
     */
    public boolean activateWindow(HWND hwnd) {
        if (hwnd == null || !user32.IsWindow(hwnd)) {
            return false;
        }
        try {
            user32.ShowWindow(hwnd, 9); // SW_RESTORE (restores and displays window)
            boolean success = user32.SetForegroundWindow(hwnd);
            if (!success) {
                // Windows bypass: simulate ALT key to unlock foreground focus
                try {
                    com.example.autoclicker.input.WindowsNativeInput.pressKey(0x12, 10);
                    success = user32.SetForegroundWindow(hwnd);
                } catch (Exception ignored) {
                }
            }
            user32.BringWindowToTop(hwnd);
            return success;
        } catch (Throwable t) {
            LoggerUtil.warn("Failed to activate target window: " + t.getMessage());
            return false;
        }
    }

    /**
     * Checks whether the current application process has Administrator elevation.
     */
    public boolean isElevated() {
        return WindowsNativeUtil.isProcessElevated();
    }
}
