package com.studydeck.dto;

public class MatchItem {

    private final int pairKey;
    private final String text;

    public MatchItem(int pairKey, String text) {
        this.pairKey = pairKey;
        this.text = text;
    }

    public int getPairKey() {
        return pairKey;
    }

    public String getText() {
        return text;
    }
}
