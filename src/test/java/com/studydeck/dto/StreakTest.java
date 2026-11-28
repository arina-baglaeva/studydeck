package com.studydeck.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StreakTest {

    @ParameterizedTest
    @CsvSource({
            "0, дней",
            "1, день",
            "2, дня",
            "4, дня",
            "5, дней",
            "11, дней",
            "14, дней",
            "21, день",
            "22, дня",
            "111, дней",
            "101, день"
    })
    void dayWordMatchesNumber(int days, String expected) {
        assertThat(new Streak(days, false).getDayWord()).isEqualTo(expected);
    }

    @Test
    void negativeDaysAreRejected() {
        assertThatThrownBy(() -> new Streak(-1, false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
