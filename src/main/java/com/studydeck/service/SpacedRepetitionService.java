package com.studydeck.service;

import com.studydeck.model.Card;
import com.studydeck.model.Grade;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SpacedRepetitionService {

    public static final int FIRST_INTERVAL_DAYS = 1;
    public static final int SECOND_INTERVAL_DAYS = 6;
    public static final double HARD_MULTIPLIER = 1.2;
    public static final double EASY_BONUS = 1.3;

    public void review(Card card, Grade grade, LocalDateTime now) {
        if (grade.isLapse()) {
            restart(card, now);
            return;
        }
        card.setEaseFactor(newEaseFactor(card.getEaseFactor(), grade));
        int interval = nextInterval(card, grade);
        card.setRepetitions(card.getRepetitions() + 1);
        card.setIntervalDays(interval);
        card.setNextReviewAt(now.plusDays(interval));
    }

    private void restart(Card card, LocalDateTime now) {
        card.setRepetitions(0);
        card.setIntervalDays(FIRST_INTERVAL_DAYS);
        card.setNextReviewAt(now.plusDays(FIRST_INTERVAL_DAYS));
    }

    static double newEaseFactor(double easeFactor, Grade grade) {
        int mistakes = 5 - grade.getQuality();
        double change = 0.1 - mistakes * (0.08 + mistakes * 0.02);
        return Math.max(Card.MIN_EASE_FACTOR, easeFactor + change);
    }

    private int nextInterval(Card card, Grade grade) {
        if (card.getRepetitions() == 0) {
            return FIRST_INTERVAL_DAYS;
        }
        int current = Math.max(1, card.getIntervalDays());
        int interval;
        if (grade == Grade.HARD) {
            interval = (int) Math.round(current * HARD_MULTIPLIER);
        } else if (card.getRepetitions() == 1) {
            interval = SECOND_INTERVAL_DAYS;
        } else {
            interval = (int) Math.round(current * card.getEaseFactor());
        }
        if (grade == Grade.EASY) {
            interval = (int) Math.round(interval * EASY_BONUS);
        }
        return Math.max(current + 1, interval);
    }
}
