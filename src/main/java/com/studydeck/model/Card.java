package com.studydeck.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
public class Card {

    public static final int TERM_MAX_LENGTH = 200;
    public static final int DEFINITION_MAX_LENGTH = 1000;
    public static final double START_EASE_FACTOR = 2.5;
    public static final double MIN_EASE_FACTOR = 1.3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = TERM_MAX_LENGTH)
    private String term;

    @Column(nullable = false, length = DEFINITION_MAX_LENGTH)
    private String definition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ColumnDefault("2.5")
    @Column(nullable = false)
    private double easeFactor = START_EASE_FACTOR;

    @ColumnDefault("0")
    @Column(nullable = false)
    private int intervalDays;

    @ColumnDefault("0")
    @Column(nullable = false)
    private int repetitions;

    @ColumnDefault("0")
    @Column(nullable = false)
    private int lapses;

    private LocalDateTime nextReviewAt;

    protected Card() {
    }

    public Card(String term, String definition) {
        this.term = term;
        this.definition = definition;
    }

    public static boolean fitsLengthLimits(String term, String definition) {
        return term != null && definition != null
                && term.length() <= TERM_MAX_LENGTH
                && definition.length() <= DEFINITION_MAX_LENGTH;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Deck getDeck() {
        return deck;
    }

    void setDeck(Deck deck) {
        this.deck = deck;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public double getEaseFactor() {
        return easeFactor;
    }

    public void setEaseFactor(double easeFactor) {
        this.easeFactor = Math.max(MIN_EASE_FACTOR, easeFactor);
    }

    public int getIntervalDays() {
        return intervalDays;
    }

    public void setIntervalDays(int intervalDays) {
        requireNotNegative(intervalDays, "intervalDays");
        this.intervalDays = intervalDays;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        requireNotNegative(repetitions, "repetitions");
        this.repetitions = repetitions;
    }

    public int getLapses() {
        return lapses;
    }

    public void setLapses(int lapses) {
        requireNotNegative(lapses, "lapses");
        this.lapses = lapses;
    }

    public LocalDateTime getNextReviewAt() {
        return nextReviewAt;
    }

    public void setNextReviewAt(LocalDateTime nextReviewAt) {
        this.nextReviewAt = nextReviewAt;
    }

    public boolean isNew() {
        return nextReviewAt == null;
    }

    public boolean isDue(LocalDateTime now) {
        return nextReviewAt != null && !nextReviewAt.isAfter(now);
    }

    private static void requireNotNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
