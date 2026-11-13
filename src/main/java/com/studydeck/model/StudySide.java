package com.studydeck.model;

public enum StudySide {

    TERM("Термин", "Определение"),
    DEFINITION("Определение", "Термин");

    private final String frontLabel;
    private final String backLabel;

    StudySide(String frontLabel, String backLabel) {
        this.frontLabel = frontLabel;
        this.backLabel = backLabel;
    }

    public String getFrontLabel() {
        return frontLabel;
    }

    public String getBackLabel() {
        return backLabel;
    }

    public String front(Card card) {
        return this == TERM ? card.getTerm() : card.getDefinition();
    }

    public String back(Card card) {
        return this == TERM ? card.getDefinition() : card.getTerm();
    }
}
