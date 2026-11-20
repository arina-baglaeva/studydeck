package com.studydeck.service;

import com.studydeck.model.Card;
import com.studydeck.model.Grade;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SpacedRepetitionServiceTest {

    private final SpacedRepetitionService service = new SpacedRepetitionService();
    private final LocalDateTime now = LocalDateTime.of(2026, 11, 20, 12, 0);

    @Test
    void goodKeepsEaseFactor() {
        assertThat(SpacedRepetitionService.newEaseFactor(2.5, Grade.GOOD)).isCloseTo(2.5, within(0.001));
    }

    @Test
    void hardLowersAndEasyRaisesEaseFactor() {
        assertThat(SpacedRepetitionService.newEaseFactor(2.5, Grade.HARD)).isCloseTo(2.36, within(0.001));
        assertThat(SpacedRepetitionService.newEaseFactor(2.5, Grade.EASY)).isCloseTo(2.6, within(0.001));
    }

    @Test
    void easeFactorNeverGoesBelowMinimum() {
        assertThat(SpacedRepetitionService.newEaseFactor(1.35, Grade.HARD)).isEqualTo(Card.MIN_EASE_FACTOR);
    }

    @Test
    void secondSuccessfulReviewGivesSixDays() {
        Card card = reviewedCard(1, 1, 2.5);

        service.review(card, Grade.GOOD, now);

        assertThat(card.getIntervalDays()).isEqualTo(6);
        assertThat(card.getRepetitions()).isEqualTo(2);
        assertThat(card.getNextReviewAt()).isEqualTo(now.plusDays(6));
    }

    @Test
    void laterReviewsMultiplyIntervalByEaseFactor() {
        Card card = reviewedCard(2, 6, 2.5);

        service.review(card, Grade.GOOD, now);

        assertThat(card.getIntervalDays()).isEqualTo(15);
        assertThat(card.getNextReviewAt()).isEqualTo(now.plusDays(15));
    }

    @Test
    void hardGrowsIntervalSlowly() {
        Card card = reviewedCard(2, 10, 2.5);

        service.review(card, Grade.HARD, now);

        assertThat(card.getIntervalDays()).isEqualTo(12);
        assertThat(card.getEaseFactor()).isCloseTo(2.36, within(0.001));
    }

    @Test
    void easyGivesLongerIntervalThanGood() {
        Card good = reviewedCard(2, 10, 2.5);
        Card easy = reviewedCard(2, 10, 2.5);

        service.review(good, Grade.GOOD, now);
        service.review(easy, Grade.EASY, now);

        assertThat(easy.getIntervalDays()).isGreaterThan(good.getIntervalDays());
    }

    @Test
    void intervalAlwaysGrowsAfterSuccess() {
        Card card = reviewedCard(3, 2, Card.MIN_EASE_FACTOR);

        service.review(card, Grade.HARD, now);

        assertThat(card.getIntervalDays()).isGreaterThan(2);
    }

    private Card reviewedCard(int repetitions, int intervalDays, double easeFactor) {
        Card card = new Card("JVM", "виртуальная машина Java");
        card.setRepetitions(repetitions);
        card.setIntervalDays(intervalDays);
        card.setEaseFactor(easeFactor);
        card.setNextReviewAt(now);
        return card;
    }
}
