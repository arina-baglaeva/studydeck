package com.studydeck.service;

import com.studydeck.dto.Streak;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StreakCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 28);

    @Test
    void noStudyDaysMeansZero() {
        Streak streak = StreakCalculator.calculate(Set.of(), TODAY);

        assertThat(streak.getDays()).isZero();
        assertThat(streak.isStudiedToday()).isFalse();
    }

    @Test
    void countsConsecutiveDaysEndingToday() {
        Set<LocalDate> days = Set.of(TODAY, TODAY.minusDays(1), TODAY.minusDays(2));

        Streak streak = StreakCalculator.calculate(days, TODAY);

        assertThat(streak.getDays()).isEqualTo(3);
        assertThat(streak.isStudiedToday()).isTrue();
    }

    @Test
    void streakIsKeptUntilEndOfTodayIfStudiedYesterday() {
        Set<LocalDate> days = Set.of(TODAY.minusDays(1), TODAY.minusDays(2));

        Streak streak = StreakCalculator.calculate(days, TODAY);

        assertThat(streak.getDays()).isEqualTo(2);
        assertThat(streak.isStudiedToday()).isFalse();
    }

    @Test
    void missedDayBreaksStreak() {
        Set<LocalDate> days = Set.of(TODAY, TODAY.minusDays(2), TODAY.minusDays(3));

        Streak streak = StreakCalculator.calculate(days, TODAY);

        assertThat(streak.getDays()).isEqualTo(1);
    }

    @Test
    void lastStudyTwoDaysAgoMeansZero() {
        Set<LocalDate> days = Set.of(TODAY.minusDays(2), TODAY.minusDays(3));

        Streak streak = StreakCalculator.calculate(days, TODAY);

        assertThat(streak.getDays()).isZero();
    }

    @Test
    void streakCrossesMonthAndYear() {
        LocalDate newYear = LocalDate.of(2027, 1, 1);
        Set<LocalDate> days = Set.of(newYear, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 12, 30));

        Streak streak = StreakCalculator.calculate(days, newYear);

        assertThat(streak.getDays()).isEqualTo(3);
    }
}
