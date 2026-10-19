package com.example.autoclicker.hotkey;

import com.example.autoclicker.util.LoggerUtil;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

/**
 * Windows-specific global hotkey listener using JNA and Win32 RegisterHotKey API.
 * Maps F8 -> START, F9 -> STOP.
 */
public class WindowsGlobalHotkeyManager implements GlobalHotkeyManager {

    private static final int HOTKEY_ID_START = 1001;
    private static final int HOTKEY_ID_STOP = 1002;
    private static final int VK_F8 = 0x77;
    private static final int VK_F9 = 0x78;

    private Thread hotkeyThread;
    private volatile int threadId = 0;
    private volatile boolean running = false;

    @Override
    public synchronized void registerHotkeys(Runnable onStart, Runnable onStop) {
        if (running) {
            return;
        }

        running = true;
        hotkeyThread = new Thread(() -> {
            threadId = Kernel32.INSTANCE.GetCurrentThreadId();
            User32 user32 = User32.INSTANCE;

            boolean regStart = user32.RegisterHotKey(null, HOTKEY_ID_START, 0, VK_F8);
            boolean regStop = user32.RegisterHotKey(null, HOTKEY_ID_STOP, 0, VK_F9);

            if (regStart && regStop) {
                LoggerUtil.info("Global hotkeys registered: [F8 = START, F9 = STOP]");
            } else {
                LoggerUtil.warn("Failed to register global hotkeys F8/F9. Another application may have reserved them.");
            }

            WinUser.MSG msg = new WinUser.MSG();
            while (running && user32.GetMessage(msg, null, 0, 0) != 0) {
                if (msg.message == WinUser.WM_HOTKEY) {
                    int id = msg.wParam.intValue();
                    if (id == HOTKEY_ID_START && onStart != null) {
                        try {
                            onStart.run();
                        } catch (Exception e) {
                            LoggerUtil.error("Error executing F8 hotkey: " + e.getMessage());
                        }
                    } else if (id == HOTKEY_ID_STOP && onStop != null) {
                        try {
                            onStop.run();
                        } catch (Exception e) {
                            LoggerUtil.error("Error executing F9 hotkey: " + e.getMessage());
                        }
                    }
                }
            }

            user32.UnregisterHotKey(null, HOTKEY_ID_START);
            user32.UnregisterHotKey(null, HOTKEY_ID_STOP);
        }, "WindowsGlobalHotkeyThread");

        hotkeyThread.setDaemon(true);
        hotkeyThread.start();
    }

    @Override
    public synchronized void unregisterHotkeys() {
        if (!running) {
            return;
        }
        running = false;
        if (threadId != 0) {
            User32.INSTANCE.PostThreadMessage(threadId, WinUser.WM_QUIT, new WinDef.WPARAM(0), new WinDef.LPARAM(0));
        }
        if (hotkeyThread != null) {
            hotkeyThread.interrupt();
        }
    }
}
