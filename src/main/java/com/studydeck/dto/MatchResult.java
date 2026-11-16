package com.studydeck.dto;

public class MatchResult {

    private final long bestTimeMillis;
    private final boolean newRecord;

    public MatchResult(long bestTimeMillis, boolean newRecord) {
        this.bestTimeMillis = bestTimeMillis;
        this.newRecord = newRecord;
    }

    public static String formatTime(long timeMillis) {
        long totalSeconds = timeMillis / 1000;
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    public long getBestTimeMillis() {
        return bestTimeMillis;
    }

    public String getBestTime() {
        return formatTime(bestTimeMillis);
    }

    public boolean isNewRecord() {
        return newRecord;
    }
}
