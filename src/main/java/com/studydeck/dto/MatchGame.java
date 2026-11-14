package com.studydeck.dto;

import java.util.List;

public class MatchGame {

    public static final int MIN_PAIRS = 2;

    private final List<MatchItem> terms;
    private final List<MatchItem> definitions;

    public MatchGame(List<MatchItem> terms, List<MatchItem> definitions) {
        this.terms = List.copyOf(terms);
        this.definitions = List.copyOf(definitions);
    }

    public List<MatchItem> getTerms() {
        return terms;
    }

    public List<MatchItem> getDefinitions() {
        return definitions;
    }

    public int getPairCount() {
        return terms.size();
    }

    public boolean isPlayable() {
        return terms.size() >= MIN_PAIRS;
    }
}
