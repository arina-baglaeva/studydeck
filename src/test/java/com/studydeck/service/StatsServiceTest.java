package com.studydeck.service;

import com.studydeck.dto.ModeStats;
import com.studydeck.model.Deck;
import com.studydeck.model.StudyMode;
import com.studydeck.model.StudySession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

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
}
