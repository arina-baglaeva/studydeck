package com.studydeck.service;

import java.util.Locale;

public final class TextUtils {

    private TextUtils() {
    }

    public static String clean(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().replaceAll("\\s+", " ");
    }

    public static String normalize(String text) {
        return clean(text).toLowerCase(Locale.ROOT);
    }

    public static String cleanOrNull(String text) {
        String cleaned = clean(text);
        return cleaned.isEmpty() ? null : cleaned;
    }
}
