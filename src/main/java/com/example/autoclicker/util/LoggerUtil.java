package com.example.autoclicker.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Utility class for logging with support for UI log observers.
 */
public final class LoggerUtil {

    private static final Logger log = LoggerFactory.getLogger("AutoClicker");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final CopyOnWriteArrayList<Consumer<String>> logListeners = new CopyOnWriteArrayList<>();

    private LoggerUtil() {
    }

    public static void addLogListener(Consumer<String> listener) {
        logListeners.add(listener);
    }

    public static void removeLogListener(Consumer<String> listener) {
        logListeners.remove(listener);
    }

    public static void info(String message) {
        log.info(message);
        notifyListeners("INFO", message);
    }

    public static void warn(String message) {
        log.warn(message);
        notifyListeners("WARN", message);
    }

    public static void error(String message) {
        log.error(message);
        notifyListeners("ERROR", message);
    }

    public static void error(String message, Throwable t) {
        log.error(message, t);
        notifyListeners("ERROR", message + " - " + t.getMessage());
    }

    private static void notifyListeners(String level, String message) {
        String formatted = String.format("%s [%s] %s", LocalTime.now().format(TIME_FORMATTER), level, message);
        for (Consumer<String> listener : logListeners) {
            try {
                listener.accept(formatted);
            } catch (Exception ignored) {
            }
        }
    }
}
