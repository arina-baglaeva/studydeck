package com.studydeck.dto;

import java.util.List;

public class ParseResult {

    private final List<ParsedCard> cards;
    private final List<Integer> badLines;
    private final List<Integer> tooLongLines;

    public ParseResult(List<ParsedCard> cards, List<Integer> badLines) {
        this(cards, badLines, List.of());
    }

    public ParseResult(List<ParsedCard> cards, List<Integer> badLines, List<Integer> tooLongLines) {
        this.cards = List.copyOf(cards);
        this.badLines = List.copyOf(badLines);
        this.tooLongLines = List.copyOf(tooLongLines);
    }

    public List<ParsedCard> getCards() {
        return cards;
    }

    public List<Integer> getBadLines() {
        return badLines;
    }

    public List<Integer> getTooLongLines() {
        return tooLongLines;
    }
}
