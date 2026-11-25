package com.studydeck.service;

import com.studydeck.model.Card;
import com.studydeck.model.Deck;
import com.studydeck.model.Grade;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SpacedRepetitionServiceTest {

    private final SpacedRepetitionService service = new SpacedRepetitionService(new MiddleRandom());
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

    @Test
    void newCardGoesToTenMinuteStep() {
        Card card = new Card("JVM", "машина");

        service.review(card, Grade.GOOD, now);

        assertThat(SpacedRepetitionService.isLearning(card)).isTrue();
        assertThat(card.getNextReviewAt()).isEqualTo(now.plusMinutes(10));
        assertThat(card.getRepetitions()).isZero();
    }

    @Test
    void goodAfterTenMinuteStepGraduatesToOneDay() {
        Card card = new Card("JVM", "машина");
        service.review(card, Grade.GOOD, now);

        service.review(card, Grade.GOOD, now.plusMinutes(10));

        assertThat(SpacedRepetitionService.isLearning(card)).isFalse();
        assertThat(card.getIntervalDays()).isEqualTo(1);
        assertThat(card.getRepetitions()).isEqualTo(1);
        assertThat(card.getNextReviewAt()).isEqualTo(now.plusMinutes(10).plusDays(1));
    }

    @Test
    void easyNewCardSkipsTenMinuteStep() {
        Card card = new Card("JVM", "машина");

        service.review(card, Grade.EASY, now);

        assertThat(card.getIntervalDays()).isEqualTo(1);
        assertThat(card.getNextReviewAt()).isEqualTo(now.plusDays(1));
    }

    @Test
    void againAndHardKeepCardOnLearningStep() {
        Card again = new Card("JVM", "машина");
        Card hard = new Card("JDK", "набор разработчика");
        service.review(again, Grade.GOOD, now);
        service.review(hard, Grade.GOOD, now);

        service.review(again, Grade.AGAIN, now);
        service.review(hard, Grade.HARD, now);

        assertThat(SpacedRepetitionService.isLearning(again)).isTrue();
        assertThat(SpacedRepetitionService.isLearning(hard)).isTrue();
        assertThat(again.getNextReviewAt()).isEqualTo(now.plusMinutes(10));
        assertThat(again.getLapses()).isZero();
        assertThat(again.getEaseFactor()).isEqualTo(2.5);
    }

    @Test
    void lapseReturnsCardToLearning() {
        Card card = reviewedCard(4, 30, 2.5);

        service.review(card, Grade.AGAIN, now);

        assertThat(SpacedRepetitionService.isLearning(card)).isTrue();
        assertThat(card.getLapses()).isEqualTo(1);
        assertThat(card.getRepetitions()).isZero();
        assertThat(card.getEaseFactor()).isCloseTo(2.3, within(0.001));
        assertThat(card.getNextReviewAt()).isEqualTo(now.plusMinutes(10));
    }

    @Test
    void lapseNeverPushesEaseFactorBelowMinimum() {
        Card card = reviewedCard(4, 30, 1.4);

        service.review(card, Grade.AGAIN, now);

        assertThat(card.getEaseFactor()).isEqualTo(Card.MIN_EASE_FACTOR);
    }

    @Test
    void lapsedCardGraduatesAgainAfterGood() {
        Card card = reviewedCard(4, 30, 2.5);
        service.review(card, Grade.AGAIN, now);

        service.review(card, Grade.GOOD, now.plusMinutes(10));

        assertThat(card.getIntervalDays()).isEqualTo(1);
        assertThat(card.getLapses()).isEqualTo(1);
    }

    private Card reviewedCard(int repetitions, int intervalDays, double easeFactor) {
        Card card = new Card("JVM", "виртуальная машина Java");
        card.setRepetitions(repetitions);
        card.setIntervalDays(intervalDays);
        card.setEaseFactor(easeFactor);
        card.setNextReviewAt(now);
        return card;
    }

    @Test
    void shortIntervalsAreNotFuzzed() {
        SpacedRepetitionService fuzzy = new SpacedRepetitionService(new Random(42));

        assertThat(fuzzy.fuzz(1)).isEqualTo(1);
        assertThat(fuzzy.fuzz(2)).isEqualTo(2);
    }

    @Test
    void fuzzStaysWithinFivePercentOrOneDay() {
        SpacedRepetitionService fuzzy = new SpacedRepetitionService(new Random(42));

        for (int i = 0; i < 200; i++) {
            assertThat(fuzzy.fuzz(6)).isBetween(5, 7);
            assertThat(fuzzy.fuzz(100)).isBetween(95, 105);
        }
    }

    @Test
    void fuzzSpreadsCardsOverDifferentDays() {
        SpacedRepetitionService fuzzy = new SpacedRepetitionService(new Random(42));
        Set<Integer> intervals = new HashSet<>();

        for (int i = 0; i < 50; i++) {
            intervals.add(fuzzy.fuzz(40));
        }

        assertThat(intervals.size()).isGreaterThan(1);
    }

    @Test
    void fuzzedIntervalStillGrowsAfterSuccess() {
        SpacedRepetitionService lowest = new SpacedRepetitionService(new LowestRandom());
        Card card = reviewedCard(3, 3, Card.MIN_EASE_FACTOR);

        lowest.review(card, Grade.HARD, now);

        assertThat(card.getIntervalDays()).isGreaterThan(3);
    }

    @Test
    void examDateCapsIntervalTwoDaysBefore() {
        Card card = reviewedCard(4, 30, 2.5);
        deckWithExam(LocalDate.of(2026, 12, 10)).addCard(card);

        service.review(card, Grade.GOOD, now);

        assertThat(card.getNextReviewAt().toLocalDate()).isEqualTo(LocalDate.of(2026, 12, 8));
        assertThat(card.getIntervalDays()).isEqualTo(18);
    }

    @Test
    void lastReviewIsOneOrTwoDaysBeforeExam() {
        LocalDate examDate = LocalDate.of(2026, 12, 10);
        SpacedRepetitionService fuzzy = new SpacedRepetitionService(new Random(7));

        for (int i = 0; i < 50; i++) {
            Card card = reviewedCard(4, 30, 2.5);
            deckWithExam(examDate).addCard(card);
            fuzzy.review(card, Grade.GOOD, now);
            assertThat(card.getNextReviewAt().toLocalDate()).isBetween(examDate.minusDays(2), examDate.minusDays(1));
        }
    }

    @Test
    void examFarAwayDoesNotChangeInterval() {
        Card card = reviewedCard(2, 6, 2.5);
        deckWithExam(LocalDate.of(2027, 6, 1)).addCard(card);

        service.review(card, Grade.GOOD, now);

        assertThat(card.getIntervalDays()).isEqualTo(15);
    }

    @Test
    void examInTwoDaysUsesDayBefore() {
        Card card = reviewedCard(2, 6, 2.5);
        deckWithExam(now.toLocalDate().plusDays(2)).addCard(card);

        service.review(card, Grade.GOOD, now);

        assertThat(card.getIntervalDays()).isEqualTo(1);
    }

    @Test
    void examTomorrowIsIgnored() {
        Card card = reviewedCard(2, 6, 2.5);
        deckWithExam(now.toLocalDate().plusDays(1)).addCard(card);

        service.review(card, Grade.GOOD, now);

        assertThat(card.getIntervalDays()).isEqualTo(15);
    }

    @Test
    void learningStepIsNotAffectedByExam() {
        Card card = new Card("JVM", "машина");
        deckWithExam(now.toLocalDate().plusDays(3)).addCard(card);

        service.review(card, Grade.GOOD, now);

        assertThat(card.getNextReviewAt()).isEqualTo(now.plusMinutes(10));
    }

    @Test
    void firstReviewTimeIsRemembered() {
        Card card = new Card("JVM", "машина");

        service.review(card, Grade.AGAIN, now);
        service.review(card, Grade.GOOD, now.plusMinutes(10));

        assertThat(card.getFirstReviewedAt()).isEqualTo(now);
    }

    @Test
    void newCardStartsWithEaseFactorTwoAndHalf() {
        Card card = new Card("JVM", "машина");

        service.review(card, Grade.GOOD, now);
        service.review(card, Grade.GOOD, now.plusMinutes(10));

        assertThat(card.getEaseFactor()).isEqualTo(Card.START_EASE_FACTOR);
    }

    @Test
    void hardOnGraduatedCardIsNotALapse() {
        Card card = reviewedCard(3, 10, 2.5);

        service.review(card, Grade.HARD, now);

        assertThat(card.getLapses()).isZero();
        assertThat(SpacedRepetitionService.isLearning(card)).isFalse();
        assertThat(card.getRepetitions()).isEqualTo(4);
    }

    @Test
    void manyHardGradesStopAtMinimumEaseFactor() {
        Card card = reviewedCard(2, 6, 2.5);

        for (int i = 0; i < 20; i++) {
            service.review(card, Grade.HARD, card.getNextReviewAt());
        }

        assertThat(card.getEaseFactor()).isEqualTo(Card.MIN_EASE_FACTOR);
    }

    @Test
    void repeatedLapsesAreCounted() {
        Card card = reviewedCard(4, 30, 2.5);

        service.review(card, Grade.AGAIN, now);
        service.review(card, Grade.GOOD, now.plusMinutes(10));
        service.review(card, Grade.AGAIN, card.getNextReviewAt());

        assertThat(card.getLapses()).isEqualTo(2);
        assertThat(card.getEaseFactor()).isCloseTo(2.1, within(0.001));
    }

    @Test
    void goodAnswersMakeIntervalsGrowStepByStep() {
        Card card = new Card("JVM", "машина");
        LocalDateTime time = now;
        service.review(card, Grade.GOOD, time);
        time = card.getNextReviewAt();
        int previous = 0;

        for (int i = 0; i < 5; i++) {
            service.review(card, Grade.GOOD, time);
            assertThat(card.getIntervalDays()).isGreaterThan(previous);
            previous = card.getIntervalDays();
            time = card.getNextReviewAt();
        }

        assertThat(previous).isGreaterThan(30);
    }

    @Test
    void reviewsBeforeExamNeverJumpOverExamDay() {
        LocalDate examDate = now.toLocalDate().plusDays(20);
        SpacedRepetitionService fuzzy = new SpacedRepetitionService(new Random(3));
        Card card = new Card("JVM", "машина");
        deckWithExam(examDate).addCard(card);
        LocalDateTime time = now;

        while (time.toLocalDate().isBefore(examDate.minusDays(1))) {
            fuzzy.review(card, Grade.EASY, time);
            time = card.getNextReviewAt();
            assertThat(time.toLocalDate()).isBefore(examDate);
        }
    }

    private Deck deckWithExam(LocalDate examDate) {
        Deck deck = new Deck("Java", null);
        deck.setExamDate(examDate);
        return deck;
    }

    private static class MiddleRandom extends Random {
        @Override
        public int nextInt(int bound) {
            return bound / 2;
        }
    }

    private static class LowestRandom extends Random {
        @Override
        public int nextInt(int bound) {
            return 0;
        }
    }
}
