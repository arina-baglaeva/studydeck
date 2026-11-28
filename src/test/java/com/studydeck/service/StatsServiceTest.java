package com.studydeck.service;

import com.studydeck.dto.DeckProgress;
import com.studydeck.dto.ModeStats;
import com.studydeck.dto.Streak;
import com.studydeck.model.Card;
import com.studydeck.model.Deck;
import com.studydeck.model.StudyMode;
import com.studydeck.model.StudySession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({StatsService.class, StudySessionService.class, DeckService.class})
class StatsServiceTest {

    @Autowired
    private StatsService statsService;

    @Autowired
    private StudySessionService studySessionService;

    @Autowired
    private DeckService deckService;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void modeStatsSumAnswersOfEachMode() {
        Deck deck = deckService.create("Java", null);
        studySessionService.recordAnswers(deck.getId(), StudyMode.QUIZ, 3, 4);
        studySessionService.recordAnswers(deck.getId(), StudyMode.QUIZ, 1, 4);
        studySessionService.recordAnswers(deck.getId(), StudyMode.WRITE, 2, 2);

        List<ModeStats> stats = statsService.modeStats();

        assertThat(stats).extracting(ModeStats::getMode)
                .containsExactly(StudyMode.QUIZ, StudyMode.WRITE, StudyMode.MATCH);
        assertThat(stats.get(0).getSessionCount()).isEqualTo(2);
        assertThat(stats.get(0).getCorrectCount()).isEqualTo(4);
        assertThat(stats.get(0).getTotalCount()).isEqualTo(8);
        assertThat(stats.get(0).getPercent()).isEqualTo(50);
        assertThat(stats.get(1).getPercent()).isEqualTo(100);
    }

    @Test
    void modeWithoutSessionsHasZeros() {
        List<ModeStats> stats = statsService.modeStats();

        assertThat(stats).hasSize(StudyMode.values().length);
        assertThat(stats).allSatisfy(item -> {
            assertThat(item.getSessionCount()).isZero();
            assertThat(item.getPercent()).isZero();
        });
    }

    @Test
    void recentSessionsAreNewestFirstAndLimitedToTen() {
        Deck deck = deckService.create("Java", null);
        for (int i = 0; i < 12; i++) {
            studySessionService.recordAnswers(deck.getId(), StudyMode.QUIZ, i, 12);
        }

        List<StudySession> recent = statsService.recentSessions();

        assertThat(recent).hasSize(10);
        assertThat(recent.get(0).getCorrectCount()).isEqualTo(11);
        assertThat(recent.get(0).getDeck().getTitle()).isEqualTo("Java");
        assertThat(statsService.totalSessions()).isEqualTo(12);
    }

    @Test
    void deckProgressCountsCardsThatLeftLearningSteps() {
        Deck deck = new Deck("Java", null);
        deck.addCard(cardWithInterval("JVM", 0));
        deck.addCard(cardWithInterval("JDK", 1));
        deck.addCard(cardWithInterval("JRE", 6));
        deck.addCard(cardWithInterval("GC", 0));
        entityManager.persist(deck);
        entityManager.flush();

        List<DeckProgress> progress = statsService.deckProgress();

        assertThat(progress).hasSize(1);
        assertThat(progress.get(0).getTitle()).isEqualTo("Java");
        assertThat(progress.get(0).getCardCount()).isEqualTo(4);
        assertThat(progress.get(0).getLearnedCount()).isEqualTo(2);
        assertThat(progress.get(0).getPercent()).isEqualTo(50);
    }

    @Test
    void emptyDeckHasZeroProgress() {
        deckService.create("Пустая", null);

        List<DeckProgress> progress = statsService.deckProgress();

        assertThat(progress).hasSize(1);
        assertThat(progress.get(0).getCardCount()).isZero();
        assertThat(progress.get(0).getLearnedCount()).isZero();
        assertThat(progress.get(0).getPercent()).isZero();
    }

    @Test
    void streakCountsDaysWithSessionsInARow() {
        Deck deck = deckService.create("Java", null);
        LocalDate today = LocalDate.of(2026, 11, 28);
        sessionFinishedAt(deck, today.atTime(9, 0));
        sessionFinishedAt(deck, today.atTime(18, 30));
        sessionFinishedAt(deck, today.minusDays(1).atTime(23, 59));
        sessionFinishedAt(deck, today.minusDays(2).atTime(0, 1));
        sessionFinishedAt(deck, today.minusDays(5).atTime(12, 0));

        Streak streak = statsService.currentStreak(today);

        assertThat(streak.getDays()).isEqualTo(3);
        assertThat(streak.isStudiedToday()).isTrue();
    }

    @Test
    void streakIsZeroWithoutSessions() {
        Streak streak = statsService.currentStreak(LocalDate.of(2026, 11, 28));

        assertThat(streak.getDays()).isZero();
        assertThat(streak.isStudiedToday()).isFalse();
    }

    private void sessionFinishedAt(Deck deck, LocalDateTime finishedAt) {
        StudySession session = entityManager.persistAndFlush(new StudySession(deck, StudyMode.QUIZ, 1, 1, null));
        entityManager.getEntityManager()
                .createQuery("update StudySession s set s.finishedAt = :finishedAt where s.id = :id")
                .setParameter("finishedAt", finishedAt)
                .setParameter("id", session.getId())
                .executeUpdate();
        entityManager.clear();
    }

    private Card cardWithInterval(String term, int intervalDays) {
        Card card = new Card(term, "определение");
        card.setIntervalDays(intervalDays);
        return card;
    }
}
