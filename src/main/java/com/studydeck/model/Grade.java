package com.studydeck.model;

public enum Grade {

    AGAIN("Снова", 2),
    HARD("Трудно", 3),
    GOOD("Хорошо", 4),
    EASY("Легко", 5);

    private final String label;
    private final int quality;

    Grade(String label, int quality) {
        this.label = label;
        this.quality = quality;
    }

    public String getLabel() {
        return label;
    }

    public int getQuality() {
        return quality;
    }

    public boolean isLapse() {
        return this == AGAIN;
    }
}
