package com.studydeck.service;

public final class TextUtils {

    private TextUtils() {
    }

    public static String clean(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().replaceAll("\\s+", " ");
    }

    public static String cleanOrNull(String text) {
        String cleaned = clean(text);
        return cleaned.isEmpty() ? null : cleaned;
    }
}
