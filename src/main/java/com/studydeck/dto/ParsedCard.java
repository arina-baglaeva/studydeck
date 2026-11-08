package com.studydeck.dto;

import java.io.Serializable;

public class ParsedCard implements Serializable {

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
