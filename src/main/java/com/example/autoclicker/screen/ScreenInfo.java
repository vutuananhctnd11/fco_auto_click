package com.example.autoclicker.screen;

import java.awt.Rectangle;

/**
 * Encapsulates metadata and physical screen bounds of a display device.
 */
public class ScreenInfo {

    private final int index;
    private final String id;
    private final String name;
    private final Rectangle bounds;
    private final boolean primary;

    public ScreenInfo(int index, String id, String name, Rectangle bounds, boolean primary) {
        this.index = index;
        this.id = id;
        this.name = name;
        this.bounds = bounds != null ? new Rectangle(bounds) : new Rectangle(0, 0, 1920, 1080);
        this.primary = primary;
    }

    public int getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    public boolean isPrimary() {
        return primary;
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
     * Converts coordinates relative to this screen into global Windows virtual desktop coordinates.
     */
    public int toGlobalX(int relativeX) {
        return bounds.x + relativeX;
    }

    /**
     * Converts coordinates relative to this screen into global Windows virtual desktop coordinates.
     */
    public int toGlobalY(int relativeY) {
        return bounds.y + relativeY;
    }

    @Override
    public String toString() {
        return String.format("Screen %d (%s) - [%dx%d at (%d, %d)]%s",
                index + 1,
                name,
                bounds.width,
                bounds.height,
                bounds.x,
                bounds.y,
                primary ? " [Primary]" : "");
    }
}
