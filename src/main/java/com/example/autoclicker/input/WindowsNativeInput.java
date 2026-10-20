package com.example.autoclicker.input;

import com.example.autoclicker.util.LoggerUtil;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.W32APIOptions;

/**
 * Native Windows input controller using Win32 API (mouse_event, keybd_event, SetCursorPos, GetCursorPos).
 * Provides hardware-like hold delay (40-60ms), hardware scan codes, smooth movement, and mouse wheel
 * required by DirectX / DirectInput / 3D games (such as FC Online / FIFA Online).
 */
public final class WindowsNativeInput {

    public static final int MOUSEEVENTF_MOVE        = 0x0001;
    public static final int MOUSEEVENTF_LEFTDOWN    = 0x0002;
    public static final int MOUSEEVENTF_LEFTUP      = 0x0004;
    public static final int MOUSEEVENTF_RIGHTDOWN   = 0x0008;
    public static final int MOUSEEVENTF_RIGHTUP     = 0x0010;
    public static final int MOUSEEVENTF_MIDDLEDOWN  = 0x0020;
    public static final int MOUSEEVENTF_MIDDLEUP    = 0x0040;
    public static final int MOUSEEVENTF_WHEEL       = 0x0800;
    public static final int MOUSEEVENTF_VIRTUALDESK = 0x4000;
    public static final int MOUSEEVENTF_ABSOLUTE    = 0x8000;
    public static final int WHEEL_DELTA             = 120;

    public static final int KEYEVENTF_KEYUP    = 0x0002;
    public static final int KEYEVENTF_SCANCODE = 0x0008;
    public static final int MAPVK_VK_TO_VSC    = 0;

    private static final BaseTSD.ULONG_PTR ZERO_EXTRA_INFO = new BaseTSD.ULONG_PTR(0);

    public interface NativeUser32 extends User32 {
        NativeUser32 INSTANCE = Native.load("user32", NativeUser32.class, W32APIOptions.DEFAULT_OPTIONS);

        void mouse_event(int dwFlags, int dx, int dy, int dwData, BaseTSD.ULONG_PTR dwExtraInfo);
        void keybd_event(byte bVk, byte bScan, int dwFlags, BaseTSD.ULONG_PTR dwExtraInfo);
        int MapVirtualKeyW(int uCode, int uMapType);
        boolean SetCursorPos(int x, int y);
        boolean GetCursorPos(WinDef.POINT pt);
    }

    private static final boolean IS_WINDOWS = Platform.isWindows();
    private static NativeUser32 user32 = null;

    static {
        if (IS_WINDOWS) {
            try {
                user32 = NativeUser32.INSTANCE;
                LoggerUtil.info("WindowsNativeInput initialized with Win32 user32 successfully.");
            } catch (Throwable t) {
                LoggerUtil.warn("Failed to initialize WindowsNativeInput: " + t.getMessage());
            }
        }
    }

    private WindowsNativeInput() {}

    public static boolean isAvailable() {
        return IS_WINDOWS && user32 != null;
    }

    public static void move(int x, int y) {
        if (isAvailable()) {
            try {
                user32.SetCursorPos(x, y);
                sendAbsoluteMouseMove(x, y);
            } catch (Exception e) {
                LoggerUtil.warn("Native SetCursorPos failed: " + e.getMessage());
            }
        }
    }

    public static void sendAbsoluteMouseMove(int x, int y) {
        if (!isAvailable()) return;
        try {
            int vLeft = user32.GetSystemMetrics(76);   // SM_XVIRTUALSCREEN
            int vTop = user32.GetSystemMetrics(77);    // SM_YVIRTUALSCREEN
            int vWidth = user32.GetSystemMetrics(78);  // SM_CXVIRTUALSCREEN
            int vHeight = user32.GetSystemMetrics(79); // SM_CYVIRTUALSCREEN
            if (vWidth <= 0 || vHeight <= 0) {
                vLeft = 0;
                vTop = 0;
                vWidth = user32.GetSystemMetrics(0);   // SM_CXSCREEN
                vHeight = user32.GetSystemMetrics(1);  // SM_CYSCREEN
            }
            if (vWidth > 1 && vHeight > 1) {
                int normX = (int) Math.round(((x - vLeft) * 65535.0) / (vWidth - 1));
                int normY = (int) Math.round(((y - vTop) * 65535.0) / (vHeight - 1));
                user32.mouse_event(MOUSEEVENTF_MOVE | MOUSEEVENTF_ABSOLUTE | MOUSEEVENTF_VIRTUALDESK, normX, normY, 0, ZERO_EXTRA_INFO);
            }
        } catch (Exception ignored) {
        }
    }

    public static void smoothMove(int targetX, int targetY, int durationMs) {
        if (!isAvailable()) {
            return;
        }
        WinDef.POINT pt = new WinDef.POINT();
        int startX = targetX;
        int startY = targetY;
        if (user32.GetCursorPos(pt)) {
            startX = pt.x;
            startY = pt.y;
        }

        int dx = targetX - startX;
        int dy = targetY - startY;
        double distance = Math.hypot(dx, dy);

        if (distance < 3) {
            move(targetX, targetY);
            return;
        }

        int steps = Math.max(6, Math.min(25, (int) (distance / 15)));
        int stepDelay = Math.max(2, durationMs / steps);

        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            // Smooth ease-out sine curve
            t = Math.sin(t * Math.PI / 2);
            int curX = (int) Math.round(startX + dx * t);
            int curY = (int) Math.round(startY + dy * t);
            user32.SetCursorPos(curX, curY);
            sendAbsoluteMouseMove(curX, curY);
            sleep(stepDelay);
        }

        user32.SetCursorPos(targetX, targetY);
        sendAbsoluteMouseMove(targetX, targetY);
    }

    public static void click(int x, int y, int holdMs) {
        if (!isAvailable()) {
            return;
        }
        smoothMove(x, y, 40);
        sleep(25); // Allow button hover state to activate in game frame loop
        user32.mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, ZERO_EXTRA_INFO);
        sleep(holdMs > 0 ? holdMs : 50);
        user32.mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, ZERO_EXTRA_INFO);
    }

    public static void doubleClick(int x, int y, int holdMs, int intervalMs) {
        click(x, y, holdMs);
        sleep(intervalMs > 0 ? intervalMs : 60);
        click(x, y, holdMs);
    }

    public static void rightClick(int x, int y, int holdMs) {
        if (!isAvailable()) {
            return;
        }
        smoothMove(x, y, 40);
        sleep(25);
        user32.mouse_event(MOUSEEVENTF_RIGHTDOWN, 0, 0, 0, ZERO_EXTRA_INFO);
        sleep(holdMs > 0 ? holdMs : 50);
        user32.mouse_event(MOUSEEVENTF_RIGHTUP, 0, 0, 0, ZERO_EXTRA_INFO);
    }

    public static void scroll(int amount) {
        if (!isAvailable()) {
            return;
        }
        user32.mouse_event(MOUSEEVENTF_WHEEL, 0, 0, amount * WHEEL_DELTA, ZERO_EXTRA_INFO);
    }

    public static void pressKey(int winVkCode, int holdMs) {
        if (!isAvailable()) {
            return;
        }
        int scanCode = user32.MapVirtualKeyW(winVkCode, MAPVK_VK_TO_VSC);
        // Press with virtual key and hardware scan code
        user32.keybd_event((byte) winVkCode, (byte) scanCode, 0, ZERO_EXTRA_INFO);
        sleep(holdMs > 0 ? holdMs : 50);
        // Release
        user32.keybd_event((byte) winVkCode, (byte) scanCode, KEYEVENTF_KEYUP, ZERO_EXTRA_INFO);
    }

    private static void sleep(int ms) {
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

