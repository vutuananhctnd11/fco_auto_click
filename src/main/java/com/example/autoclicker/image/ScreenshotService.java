package com.example.autoclicker.image;

import com.example.autoclicker.automation.CoordinateMode;
import com.example.autoclicker.screen.CaptureRegion;
import com.example.autoclicker.screen.ScreenInfo;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

/**
 * Service to capture screenshots of targeted screen regions using java.awt.Robot.
 * Resolves capture regions according to CoordinateMode (Window-relative or Screen-absolute).
 */
public class ScreenshotService {

    private final Robot robot;

    public ScreenshotService() {
        try {
            this.robot = new Robot();
        } catch (AWTException e) {
            throw new IllegalStateException("Failed to initialize java.awt.Robot for screenshot capture", e);
        }
    }

    /**
     * Captures a region given in absolute virtual screen coordinates.
     */
    public BufferedImage capture(CaptureRegion region) {
        if (region == null || !region.isValid()) {
            throw new IllegalArgumentException("Invalid capture region: " + region);
        }
        return robot.createScreenCapture(new Rectangle(region.getX(), region.getY(), region.getWidth(), region.getHeight()));
    }

    /**
     * Captures a region relative to the specified screen or window bounds.
     */
    public BufferedImage capture(ScreenInfo screen, CaptureRegion region, Rectangle windowBounds) {
        if (region == null || !region.isValid()) {
            throw new IllegalArgumentException("Invalid capture region: " + region);
        }

        int absX;
        int absY;

        if (region.getCoordinateMode() == CoordinateMode.WINDOW_RELATIVE && windowBounds != null) {
            absX = windowBounds.x + region.getX();
            absY = windowBounds.y + region.getY();
        } else if (screen != null) {
            absX = screen.toGlobalX(region.getX());
            absY = screen.toGlobalY(region.getY());
        } else {
            absX = region.getX();
            absY = region.getY();
        }

        Rectangle captureBounds = new Rectangle(absX, absY, region.getWidth(), region.getHeight());
        return robot.createScreenCapture(captureBounds);
    }

    /**
     * Backward-compatible overload.
     */
    public BufferedImage capture(ScreenInfo screen, CaptureRegion relativeRegion) {
        return capture(screen, relativeRegion, null);
    }

    /**
     * Captures the full bounds of the given screen.
     */
    public BufferedImage captureScreen(ScreenInfo screen) {
        if (screen == null) {
            throw new IllegalArgumentException("ScreenInfo cannot be null");
        }
        return robot.createScreenCapture(screen.getBounds());
    }

    /**
     * Captures the full bounds of the given window.
     */
    public BufferedImage captureWindow(Rectangle windowBounds) {
        if (windowBounds == null || windowBounds.width <= 0 || windowBounds.height <= 0) {
            throw new IllegalArgumentException("Invalid window bounds for capture");
        }
        return robot.createScreenCapture(windowBounds);
    }
}
