package com.studydeck.model;

public enum StudyMode {

    QUIZ("Тест"),
    WRITE("Напиши ответ"),
    MATCH("Соедини");

    private final String label;

    StudyMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
