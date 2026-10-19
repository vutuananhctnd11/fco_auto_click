package com.example.autoclicker.input;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

/**
 * Sends mouse clicks to a specific Windows HWND using PostMessage without taking over the physical cursor.
 */
public class BackgroundMouseService {

    private static final int WM_MOUSEMOVE = 0x0200;
    private static final int WM_LBUTTONDOWN = 0x0201;
    private static final int WM_LBUTTONUP = 0x0202;
    private static final int WM_RBUTTONDOWN = 0x0204;
    private static final int WM_RBUTTONUP = 0x0205;
    private static final int MK_LBUTTON = 0x0001;
    private static final int MK_RBUTTON = 0x0002;

    private static WinDef.LPARAM makeLParam(int x, int y) {
        return new WinDef.LPARAM(((long) (y & 0xFFFF) << 16) | (x & 0xFFFF));
    }

    public static boolean click(WinDef.HWND hwnd, int clientX, int clientY) {
        if (hwnd == null) {
            return false;
        }
        User32 user32 = User32.INSTANCE;
        WinDef.LPARAM lParam = makeLParam(clientX, clientY);

        user32.PostMessage(hwnd, WM_MOUSEMOVE, new WinDef.WPARAM(0), lParam);
        user32.PostMessage(hwnd, WM_LBUTTONDOWN, new WinDef.WPARAM(MK_LBUTTON), lParam);
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        user32.PostMessage(hwnd, WM_LBUTTONUP, new WinDef.WPARAM(0), lParam);
        return true;
    }

    public static boolean doubleClick(WinDef.HWND hwnd, int clientX, int clientY, int intervalMs) {
        if (hwnd == null) {
            return false;
        }
        click(hwnd, clientX, clientY);
        try {
            Thread.sleep(intervalMs > 0 ? intervalMs : 50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        click(hwnd, clientX, clientY);
        return true;
    }

    public static boolean rightClick(WinDef.HWND hwnd, int clientX, int clientY) {
        if (hwnd == null) {
            return false;
        }
        User32 user32 = User32.INSTANCE;
        WinDef.LPARAM lParam = makeLParam(clientX, clientY);

        user32.PostMessage(hwnd, WM_MOUSEMOVE, new WinDef.WPARAM(0), lParam);
        user32.PostMessage(hwnd, WM_RBUTTONDOWN, new WinDef.WPARAM(MK_RBUTTON), lParam);
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        user32.PostMessage(hwnd, WM_RBUTTONUP, new WinDef.WPARAM(0), lParam);
        return true;
    }
}
