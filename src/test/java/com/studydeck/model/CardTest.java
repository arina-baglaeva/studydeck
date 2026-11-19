package com.studydeck.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardTest {

    private final LocalDateTime now = LocalDateTime.of(2026, 11, 19, 12, 0);

    @Test
    void newCardHasStartingReviewValues() {
        Card card = new Card("JVM", "виртуальная машина Java");

        assertThat(card.getEaseFactor()).isEqualTo(2.5);
        assertThat(card.getIntervalDays()).isZero();
        assertThat(card.getRepetitions()).isZero();
        assertThat(card.getLapses()).isZero();
        assertThat(card.getNextReviewAt()).isNull();
        assertThat(card.isNew()).isTrue();
    }

    @Test
    void easeFactorNeverGoesBelowMinimum() {
        Card card = new Card("JVM", "машина");

        card.setEaseFactor(1.1);

        assertThat(card.getEaseFactor()).isEqualTo(Card.MIN_EASE_FACTOR);
    }

    @Test
    void easeFactorAboveMinimumIsKept() {
        Card card = new Card("JVM", "машина");

        card.setEaseFactor(2.36);

        assertThat(card.getEaseFactor()).isEqualTo(2.36);
    }

    @Test
    void negativeCountersAreRejected() {
        Card card = new Card("JVM", "машина");

        assertThatThrownBy(() -> card.setIntervalDays(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> card.setRepetitions(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> card.setLapses(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cardIsDueWhenReviewTimeHasCome() {
        Card card = new Card("JVM", "машина");

        card.setNextReviewAt(now);

        assertThat(card.isNew()).isFalse();
        assertThat(card.isDue(now)).isTrue();
        assertThat(card.isDue(now.plusDays(1))).isTrue();
        assertThat(card.isDue(now.minusMinutes(1))).isFalse();
    }

    @Test
    void newCardIsNotDue() {
        Card card = new Card("JVM", "машина");

        assertThat(card.isDue(now)).isFalse();
    }
}
