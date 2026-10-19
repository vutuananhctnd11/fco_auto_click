package com.example.autoclicker.screen;

import com.example.autoclicker.automation.CoordinateMode;
import java.awt.Rectangle;
import java.util.Objects;

/**
 * Represents a region on a screen to capture or inspect.
 */
public class CaptureRegion {

    private int x;
    private int y;
    private int width;
    private int height;
    private CoordinateMode coordinateMode = CoordinateMode.WINDOW_RELATIVE;

    public CaptureRegion() {
        this(0, 0, 100, 100, CoordinateMode.WINDOW_RELATIVE);
    }

    public CaptureRegion(int x, int y, int width, int height) {
        this(x, y, width, height, CoordinateMode.WINDOW_RELATIVE);
    }

    public CaptureRegion(int x, int y, int width, int height, CoordinateMode coordinateMode) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.coordinateMode = coordinateMode != null ? coordinateMode : CoordinateMode.WINDOW_RELATIVE;
    }

    public CoordinateMode getCoordinateMode() {
        return coordinateMode;
    }

    public void setCoordinateMode(CoordinateMode coordinateMode) {
        this.coordinateMode = coordinateMode != null ? coordinateMode : CoordinateMode.WINDOW_RELATIVE;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Rectangle toRectangle() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isValid() {
        return width > 0 && height > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaptureRegion that = (CaptureRegion) o;
        return x == that.x && y == that.y && width == that.width && height == that.height && coordinateMode == that.coordinateMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height, coordinateMode);
    }

    @Override
    public String toString() {
        return String.format("CaptureRegion[x=%d, y=%d, w=%d, h=%d, mode=%s]", x, y, width, height, coordinateMode);
    }
}
