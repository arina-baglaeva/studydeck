package com.studydeck.dto;

import com.studydeck.model.StudyMode;

public class ModeStats {

    private final StudyMode mode;
    private final long sessionCount;
    private final long correctCount;
    private final long totalCount;

    public ModeStats(StudyMode mode, long sessionCount, long correctCount, long totalCount) {
        this.mode = mode;
        this.sessionCount = sessionCount;
        this.correctCount = correctCount;
        this.totalCount = totalCount;
    }

    public static ModeStats empty(StudyMode mode) {
        return new ModeStats(mode, 0, 0, 0);
    }

    public StudyMode getMode() {
        return mode;
    }

    public long getSessionCount() {
        return sessionCount;
    }

    public long getCorrectCount() {
        return correctCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public int getPercent() {
        if (totalCount == 0) {
            return 0;
        }
        return (int) (correctCount * 100 / totalCount);
    }
}
