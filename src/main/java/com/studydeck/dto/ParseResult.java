package com.studydeck.dto;

import java.util.List;

public class ParseResult {

    private final List<ParsedCard> cards;
    private final List<Integer> badLines;

    public ParseResult(List<ParsedCard> cards, List<Integer> badLines) {
        this.cards = List.copyOf(cards);
        this.badLines = List.copyOf(badLines);
    }

    public List<ParsedCard> getCards() {
        return cards;
    }

    public List<Integer> getBadLines() {
        return badLines;
    }
}
