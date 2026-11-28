package com.studydeck.service;

import com.studydeck.dto.Streak;

import java.time.LocalDate;
import java.util.Set;

public final class StreakCalculator {

    private StreakCalculator() {
    }

    public static Streak calculate(Set<LocalDate> studyDays, LocalDate today) {
        boolean studiedToday = studyDays.contains(today);
        LocalDate day = studiedToday ? today : today.minusDays(1);
        int count = 0;
        while (studyDays.contains(day)) {
            count++;
            day = day.minusDays(1);
        }
        return new Streak(count, studiedToday);
    }
}
