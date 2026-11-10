package com.studydeck.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ImportPreview implements Serializable {

    private final Long deckId;
    private final List<ParsedCard> cards;
    private final List<Integer> badLines;
    private final List<Integer> tooLongLines;
    private final int duplicateCount;

    public ImportPreview(Long deckId, List<ParsedCard> cards, List<Integer> badLines, int duplicateCount) {
        this(deckId, cards, badLines, List.of(), duplicateCount);
    }

    public ImportPreview(Long deckId, List<ParsedCard> cards, List<Integer> badLines,
                         List<Integer> tooLongLines, int duplicateCount) {
        this.deckId = deckId;
        this.cards = List.copyOf(cards);
        this.badLines = List.copyOf(badLines);
        this.tooLongLines = List.copyOf(tooLongLines);
        this.duplicateCount = duplicateCount;
    }

    public Long getDeckId() {
        return deckId;
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

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public boolean belongsTo(Long otherDeckId) {
        return deckId != null && deckId.equals(otherDeckId);
    }

    public List<ParsedCard> selectCards(List<Integer> indexes) {
        Set<Integer> validIndexes = new TreeSet<>();
        if (indexes != null) {
            for (Integer index : indexes) {
                if (index != null && index >= 0 && index < cards.size()) {
                    validIndexes.add(index);
                }
            }
        }
        List<ParsedCard> selected = new ArrayList<>();
        for (Integer index : validIndexes) {
            selected.add(cards.get(index));
        }
        return selected;
    }
}
