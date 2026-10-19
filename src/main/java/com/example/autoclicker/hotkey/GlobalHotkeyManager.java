package com.example.autoclicker.hotkey;

/**
 * Interface for system-wide global hotkey listeners.
 */
public interface GlobalHotkeyManager {

    /**
     * Registers actions for START and STOP hotkeys.
     *
     * @param onStart Callback triggered when the start hotkey is pressed (e.g. F8)
     * @param onStop  Callback triggered when the stop hotkey is pressed (e.g. F9)
     */
    void registerHotkeys(Runnable onStart, Runnable onStop);

    /**
     * Unregisters all active hotkeys and cleans up resources.
     */
    void unregisterHotkeys();
}
