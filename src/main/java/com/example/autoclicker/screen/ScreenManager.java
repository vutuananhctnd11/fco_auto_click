package com.example.autoclicker.screen;

import com.example.autoclicker.util.LoggerUtil;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages detection and coordinate resolution across all connected monitors.
 */
public class ScreenManager {

    private final List<ScreenInfo> screens = new ArrayList<>();

    public ScreenManager() {
        refreshScreens();
    }

    /**
     * Refreshes the list of active screens using AWT GraphicsEnvironment.
     */
    public synchronized void refreshScreens() {
        screens.clear();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        GraphicsDevice defaultDevice = ge.getDefaultScreenDevice();

        for (int i = 0; i < devices.length; i++) {
            GraphicsDevice gd = devices[i];
            Rectangle bounds = gd.getDefaultConfiguration().getBounds();
            boolean isPrimary = gd.equals(defaultDevice) || (bounds.x == 0 && bounds.y == 0);
            ScreenInfo info = new ScreenInfo(i, gd.getIDstring(), "Display " + (i + 1), bounds, isPrimary);
            screens.add(info);
            LoggerUtil.info("Detected " + info);
        }
    }

    public List<ScreenInfo> getScreens() {
        return Collections.unmodifiableList(screens);
    }

    public ScreenInfo getScreen(int index) {
        if (index >= 0 && index < screens.size()) {
            return screens.get(index);
        }
        return screens.isEmpty() ? null : screens.get(0);
    }

    public ScreenInfo getPrimaryScreen() {
        for (ScreenInfo s : screens) {
            if (s.isPrimary()) {
                return s;
            }
        }
        return screens.isEmpty() ? null : screens.get(0);
    }

    public int getScreenCount() {
        return screens.size();
    }
}
