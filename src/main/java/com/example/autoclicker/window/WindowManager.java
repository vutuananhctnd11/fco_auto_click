package com.example.autoclicker.window;

import com.example.autoclicker.util.LoggerUtil;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages detection, enumeration and bounds tracking for active Windows application windows.
 */
public class WindowManager {

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
                    WinDef.RECT rect = new WinDef.RECT();
                    if (user32.GetWindowRect(hwnd, rect)) {
                        int w = rect.right - rect.left;
                        int h = rect.bottom - rect.top;
                        // Filter out non-interactive micro-windows / tooltips
                        if (w > 50 && h > 50 && !title.equalsIgnoreCase("Program Manager")) {
                            result.add(new WindowInfo(hwnd, title, new Rectangle(rect.left, rect.top, w, h)));
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
     */
    public Rectangle getWindowBounds(HWND hwnd) {
        if (hwnd == null || !user32.IsWindow(hwnd)) {
            return null;
        }
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
     * Restores, brings to top, and activates the window so it receives keyboard and mouse input.
     */
    public boolean activateWindow(HWND hwnd) {
        if (hwnd == null || !user32.IsWindow(hwnd)) {
            return false;
        }
        try {
            user32.ShowWindow(hwnd, 9); // SW_RESTORE
            return user32.SetForegroundWindow(hwnd);
        } catch (Throwable t) {
            LoggerUtil.warn("Failed to activate target window: " + t.getMessage());
            return false;
        }
    }
}
