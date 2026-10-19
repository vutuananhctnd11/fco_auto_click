package com.example.autoclicker.window;

import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.Rectangle;

/**
 * Information and boundary coordinates of an operating system window.
 */
public class WindowInfo {

    private final HWND hwnd;
    private final String title;
    private Rectangle bounds;

    public WindowInfo(HWND hwnd, String title, Rectangle bounds) {
        this.hwnd = hwnd;
        this.title = title != null ? title.trim() : "";
        this.bounds = bounds != null ? new Rectangle(bounds) : new Rectangle(0, 0, 0, 0);
    }

    public HWND getHwnd() {
        return hwnd;
    }

    public long getHwndVal() {
        return hwnd != null ? com.sun.jna.Pointer.nativeValue(hwnd.getPointer()) : 0L;
    }

    public String getTitle() {
        return title;
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds != null ? new Rectangle(bounds) : new Rectangle(0, 0, 0, 0);
    }

    public int getX() {
        return bounds.x;
    }

    public int getY() {
        return bounds.y;
    }

    public int getWidth() {
        return bounds.width;
    }

    public int getHeight() {
        return bounds.height;
    }

    /**
     * Converts a window-relative X coordinate to global virtual desktop coordinate.
     */
    public int toGlobalX(int relativeX) {
        return bounds.x + relativeX;
    }

    /**
     * Converts a window-relative Y coordinate to global virtual desktop coordinate.
     */
    public int toGlobalY(int relativeY) {
        return bounds.y + relativeY;
    }

    @Override
    public String toString() {
        return String.format("%s [%dx%d at (%d, %d)]", title, bounds.width, bounds.height, bounds.x, bounds.y);
    }
}
