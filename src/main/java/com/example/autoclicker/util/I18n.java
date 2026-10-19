package com.example.autoclicker.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Internationalization manager supporting dynamic language switching between English and Vietnamese.
 * Ensures UTF-8 encoding across all operating systems.
 */
public final class I18n {

    public static final Locale LOCALE_EN = Locale.ENGLISH;
    public static final Locale LOCALE_VI = Locale.of("vi", "VN");

    private static Locale currentLocale = LOCALE_EN;
    private static ResourceBundle bundle = loadBundle(currentLocale);
    private static final CopyOnWriteArrayList<Consumer<Locale>> listeners = new CopyOnWriteArrayList<>();

    private I18n() {
    }

    private static ResourceBundle loadBundle(Locale locale) {
        String baseName = "i18n/messages";
        String bundleName = locale.getLanguage().equalsIgnoreCase("vi") ? baseName + "_vi.properties" : baseName + "_en.properties";

        try (InputStream in = I18n.class.getClassLoader().getResourceAsStream(bundleName)) {
            if (in != null) {
                return new PropertyResourceBundle(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
        }
        return ResourceBundle.getBundle("i18n.messages", locale);
    }

    public static synchronized void setLocale(Locale locale) {
        if (locale == null) {
            return;
        }
        currentLocale = locale;
        bundle = loadBundle(currentLocale);
        for (Consumer<Locale> listener : listeners) {
            try {
                listener.accept(currentLocale);
            } catch (Exception ignored) {
            }
        }
    }

    public static synchronized Locale getLocale() {
        return currentLocale;
    }

    public static synchronized ResourceBundle getBundle() {
        return bundle;
    }

    public static synchronized String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    public static synchronized String format(String key, Object... args) {
        try {
            String pattern = bundle.getString(key);
            return MessageFormat.format(pattern, args);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    public static void addListener(Consumer<Locale> listener) {
        listeners.add(listener);
    }

    public static void removeListener(Consumer<Locale> listener) {
        listeners.remove(listener);
    }
}
