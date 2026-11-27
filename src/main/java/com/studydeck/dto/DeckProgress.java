package com.studydeck.dto;

public class DeckProgress {

    private final Long deckId;
    private final String title;
    private final long cardCount;
    private final long learnedCount;

    public DeckProgress(Long deckId, String title, long cardCount, long learnedCount) {
        this.deckId = deckId;
        this.title = title;
        this.cardCount = cardCount;
        this.learnedCount = learnedCount;
    }

    public Long getDeckId() {
        return deckId;
    }

    public String getTitle() {
        return title;
    }

    public long getCardCount() {
        return cardCount;
    }

    public long getLearnedCount() {
        return learnedCount;
    }

    public int getPercent() {
        if (cardCount == 0) {
            return 0;
        }
        return (int) (learnedCount * 100 / cardCount);
    }
}
