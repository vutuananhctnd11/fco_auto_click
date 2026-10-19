package com.example.autoclicker.automation;

import com.example.autoclicker.screen.CaptureRegion;

import java.io.File;
import java.util.Objects;

/**
 * Model representing a single automation step within a branch.
 * Supports CLICK, DOUBLE_CLICK, RIGHT_CLICK, KEY_PRESS, WAIT and CHECK_IMAGE.
 */
public class AutomationAction {

    private AutomationActionType type;
    private int x;
    private int y;
    private int width;
    private int height;
    private String keyName;
    private int delayMs;
    private CoordinateMode coordinateMode = CoordinateMode.WINDOW_RELATIVE;

    // Parameters for CHECK_IMAGE
    private String templatePath = "";
    private double threshold = 0.90;

    public AutomationAction() {
        this.type = AutomationActionType.CLICK;
        this.x = 0;
        this.y = 0;
        this.width = 100;
        this.height = 100;
        this.keyName = "";
        this.delayMs = 300;
        this.coordinateMode = CoordinateMode.WINDOW_RELATIVE;
    }

    public AutomationAction(AutomationActionType type, int x, int y, int delayMs) {
        this(type, x, y, delayMs, CoordinateMode.WINDOW_RELATIVE);
    }

    public AutomationAction(AutomationActionType type, int x, int y, int delayMs, CoordinateMode coordinateMode) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = 100;
        this.height = 100;
        this.keyName = "";
        this.delayMs = delayMs;
        this.coordinateMode = coordinateMode != null ? coordinateMode : CoordinateMode.WINDOW_RELATIVE;
    }

    public AutomationAction(AutomationActionType type, String keyName, int delayMs) {
        this.type = type;
        this.x = 0;
        this.y = 0;
        this.width = 100;
        this.height = 100;
        this.keyName = keyName;
        this.delayMs = delayMs;
        this.coordinateMode = CoordinateMode.WINDOW_RELATIVE;
    }

    public AutomationAction(AutomationAction other) {
        this.type = other.type;
        this.x = other.x;
        this.y = other.y;
        this.width = other.width;
        this.height = other.height;
        this.keyName = other.keyName;
        this.delayMs = other.delayMs;
        this.coordinateMode = other.coordinateMode != null ? other.coordinateMode : CoordinateMode.WINDOW_RELATIVE;
        this.templatePath = other.templatePath != null ? other.templatePath : "";
        this.threshold = other.threshold;
    }

    public CoordinateMode getCoordinateMode() {
        return coordinateMode;
    }

    public void setCoordinateMode(CoordinateMode coordinateMode) {
        this.coordinateMode = coordinateMode != null ? coordinateMode : CoordinateMode.WINDOW_RELATIVE;
    }

    public AutomationActionType getType() {
        return type;
    }

    public void setType(AutomationActionType type) {
        this.type = type;
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

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public int getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(int delayMs) {
        this.delayMs = delayMs;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath != null ? templatePath : "";
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public CaptureRegion getCaptureRegion() {
        return new CaptureRegion(x, y, width, height, coordinateMode);
    }

    public void setCaptureRegion(CaptureRegion region) {
        if (region != null) {
            this.x = region.getX();
            this.y = region.getY();
            this.width = region.getWidth();
            this.height = region.getHeight();
            this.coordinateMode = region.getCoordinateMode();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutomationAction that = (AutomationAction) o;
        return x == that.x && y == that.y && width == that.width && height == that.height
                && delayMs == that.delayMs && Double.compare(threshold, that.threshold) == 0
                && type == that.type && Objects.equals(keyName, that.keyName)
                && coordinateMode == that.coordinateMode && Objects.equals(templatePath, that.templatePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, x, y, width, height, keyName, delayMs, coordinateMode, templatePath, threshold);
    }

    @Override
    public String toString() {
        String modeStr = coordinateMode == CoordinateMode.WINDOW_RELATIVE ? "WinRel" : "Abs";
        if (type == AutomationActionType.KEY_PRESS) {
            return String.format("%-12s | Key: %-6s | Delay: %dms", type, keyName, delayMs);
        } else if (type == AutomationActionType.WAIT) {
            return String.format("%-12s | Delay: %dms", type, delayMs);
        } else if (type == AutomationActionType.CHECK_IMAGE) {
            String filename = templatePath.isBlank() ? "None" : new File(templatePath).getName();
            return String.format("%-12s | [%d,%d %dx%d %s] | Thresh: %.2f | Tpl: %s",
                    type, x, y, width, height, modeStr, threshold, filename);
        } else {
            return String.format("%-12s | (%d, %d) [%s] | Delay: %dms", type, x, y, modeStr, delayMs);
        }
    }
}
