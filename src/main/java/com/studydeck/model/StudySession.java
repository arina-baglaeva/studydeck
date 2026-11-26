package com.studydeck.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_sessions")
public class StudySession {

    public static final int MODE_MAX_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Deck deck;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = MODE_MAX_LENGTH)
    private StudyMode mode;

    @Column(nullable = false)
    private int correctCount;

    @Column(nullable = false)
    private int totalCount;

    @Column
    private Long durationMillis;

    @Column(nullable = false, updatable = false)
    private LocalDateTime finishedAt;

    protected StudySession() {
    }

    public StudySession(Deck deck, StudyMode mode, int correctCount, int totalCount, Long durationMillis) {
        if (deck == null || mode == null) {
            throw new IllegalArgumentException("Нужны колода и режим");
        }
        if (totalCount < 0 || correctCount < 0 || correctCount > totalCount) {
            throw new IllegalArgumentException("Неверный результат");
        }
        if (durationMillis != null && durationMillis < 0) {
            throw new IllegalArgumentException("Неверное время");
        }
        this.deck = deck;
        this.mode = mode;
        this.correctCount = correctCount;
        this.totalCount = totalCount;
        this.durationMillis = durationMillis;
    }

    @PrePersist
    void onCreate() {
        finishedAt = LocalDateTime.now();
    }

    public int getPercent() {
        if (totalCount == 0) {
            return 0;
        }
        return correctCount * 100 / totalCount;
    }

    public Long getId() {
        return id;
    }

    public Deck getDeck() {
        return deck;
    }

    public StudyMode getMode() {
        return mode;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public Long getDurationMillis() {
        return durationMillis;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }
}
