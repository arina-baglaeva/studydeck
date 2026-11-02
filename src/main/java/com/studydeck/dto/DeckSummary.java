package com.studydeck.dto;

public class DeckSummary {

    private final Long id;
    private final String title;
    private final String description;
    private final long cardCount;

    public DeckSummary(Long id, String title, String description, long cardCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.cardCount = cardCount;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getCardCount() {
        return cardCount;
    }
}
