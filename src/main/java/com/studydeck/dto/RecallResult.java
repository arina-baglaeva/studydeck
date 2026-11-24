package com.studydeck.dto;

public class RecallResult {

    private final String typedAnswer;
    private final boolean matches;

    public RecallResult(String typedAnswer, boolean matches) {
        this.typedAnswer = typedAnswer;
        this.matches = matches;
    }

    public static RecallResult notTyped() {
        return new RecallResult("", false);
    }

    public String getTypedAnswer() {
        return typedAnswer;
    }

    public boolean isTyped() {
        return !typedAnswer.isEmpty();
    }

    public boolean isMatches() {
        return matches;
    }
}
