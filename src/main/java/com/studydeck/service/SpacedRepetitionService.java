package com.studydeck.service;

import com.studydeck.model.Card;
import com.studydeck.model.Grade;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
public class SpacedRepetitionService {

    public static final int LEARNING_STEP_MINUTES = 10;
    public static final int GRADUATING_INTERVAL_DAYS = 1;
    public static final int SECOND_INTERVAL_DAYS = 6;
    public static final double HARD_MULTIPLIER = 1.2;
    public static final double EASY_BONUS = 1.3;
    public static final double LAPSE_EASE_PENALTY = 0.2;
    public static final int MIN_FUZZ_INTERVAL_DAYS = 3;
    public static final double FUZZ_FACTOR = 0.05;
    public static final int MIN_DAYS_BEFORE_EXAM = 1;
    public static final int MAX_DAYS_BEFORE_EXAM = 2;

    private final Random random;

    public SpacedRepetitionService() {
        this(new Random());
    }

    SpacedRepetitionService(Random random) {
        this.random = random;
    }

    public void review(Card card, Grade grade, LocalDateTime now) {
        if (isLearning(card)) {
            learn(card, grade, now);
        } else if (grade.isLapse()) {
            lapse(card, now);
        } else {
            reviewGraduated(card, grade, now);
        }
    }

    public static boolean isLearning(Card card) {
        return card.getIntervalDays() == 0;
    }

    private void learn(Card card, Grade grade, LocalDateTime now) {
        boolean graduates = grade == Grade.EASY || (grade == Grade.GOOD && !card.isNew());
        if (graduates) {
            card.setRepetitions(1);
            scheduleInDays(card, GRADUATING_INTERVAL_DAYS, now);
        } else {
            card.setNextReviewAt(now.plusMinutes(LEARNING_STEP_MINUTES));
        }
    }

    private void lapse(Card card, LocalDateTime now) {
        card.setLapses(card.getLapses() + 1);
        card.setEaseFactor(card.getEaseFactor() - LAPSE_EASE_PENALTY);
        card.setRepetitions(0);
        card.setIntervalDays(0);
        card.setNextReviewAt(now.plusMinutes(LEARNING_STEP_MINUTES));
    }

    private void reviewGraduated(Card card, Grade grade, LocalDateTime now) {
        card.setEaseFactor(newEaseFactor(card.getEaseFactor(), grade));
        int interval = Math.max(card.getIntervalDays() + 1, fuzz(nextInterval(card, grade)));
        card.setRepetitions(card.getRepetitions() + 1);
        scheduleInDays(card, interval, now);
    }

    private void scheduleInDays(Card card, int interval, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalDate lastDay = lastReviewDayBeforeExam(card, today);
        if (lastDay != null && today.plusDays(interval).isAfter(lastDay)) {
            interval = (int) ChronoUnit.DAYS.between(today, lastDay);
        }
        card.setIntervalDays(interval);
        card.setNextReviewAt(now.plusDays(interval));
    }

    LocalDate lastReviewDayBeforeExam(Card card, LocalDate today) {
        if (card.getDeck() == null || card.getDeck().getExamDate() == null) {
            return null;
        }
        LocalDate examDate = card.getDeck().getExamDate();
        int extraDays = random.nextInt(MAX_DAYS_BEFORE_EXAM - MIN_DAYS_BEFORE_EXAM + 1);
        LocalDate lastDay = examDate.minusDays(MIN_DAYS_BEFORE_EXAM + extraDays);
        if (!lastDay.isAfter(today)) {
            lastDay = examDate.minusDays(MIN_DAYS_BEFORE_EXAM);
        }
        if (!lastDay.isAfter(today)) {
            return null;
        }
        return lastDay;
    }

    static double newEaseFactor(double easeFactor, Grade grade) {
        int mistakes = 5 - grade.getQuality();
        double change = 0.1 - mistakes * (0.08 + mistakes * 0.02);
        return Math.max(Card.MIN_EASE_FACTOR, easeFactor + change);
    }

    private int nextInterval(Card card, Grade grade) {
        int current = card.getIntervalDays();
        int interval;
        if (grade == Grade.HARD) {
            interval = (int) Math.round(current * HARD_MULTIPLIER);
        } else if (card.getRepetitions() <= 1) {
            interval = SECOND_INTERVAL_DAYS;
        } else {
            interval = (int) Math.round(current * card.getEaseFactor());
        }
        if (grade == Grade.EASY) {
            interval = (int) Math.round(interval * EASY_BONUS);
        }
        return Math.max(current + 1, interval);
    }

    int fuzz(int interval) {
        if (interval < MIN_FUZZ_INTERVAL_DAYS) {
            return interval;
        }
        int range = Math.max(1, (int) Math.round(interval * FUZZ_FACTOR));
        return interval - range + random.nextInt(2 * range + 1);
    }
}
