package com.studydeck.dto;

public class ParsedCard {

    private final String term;
    private final String definition;

    public ParsedCard(String term, String definition) {
        this.term = term;
        this.definition = definition;
    }

    public String getTerm() {
        return term;
    }

    public String getDefinition() {
        return definition;
    }
}
