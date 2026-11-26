package com.studydeck.service;

import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Deck;
import com.studydeck.model.StudyMode;
import com.studydeck.model.StudySession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({StudySessionService.class, DeckService.class})
class StudySessionServiceTest {

    @Autowired
    private StudySessionService studySessionService;

    @Autowired
    private DeckService deckService;

    @Test
    void recordsQuizAnswers() {
        Deck deck = deckService.create("Java", null);

        StudySession session = studySessionService.recordAnswers(deck.getId(), StudyMode.QUIZ, 7, 10);

        assertThat(session.getMode()).isEqualTo(StudyMode.QUIZ);
        assertThat(session.getCorrectCount()).isEqualTo(7);
        assertThat(session.getTotalCount()).isEqualTo(10);
        assertThat(session.getDurationMillis()).isNull();
        assertThat(session.getPercent()).isEqualTo(70);
    }

    @Test
    void recordsMatchWithTimeAndAllPairsFound() {
        Deck deck = deckService.create("Java", null);

        StudySession session = studySessionService.recordMatch(deck.getId(), 6, 35_000);

        assertThat(session.getMode()).isEqualTo(StudyMode.MATCH);
        assertThat(session.getCorrectCount()).isEqualTo(6);
        assertThat(session.getTotalCount()).isEqualTo(6);
        assertThat(session.getDurationMillis()).isEqualTo(35_000L);
    }

    @Test
    void findsSessionsOfDeck() {
        Deck java = deckService.create("Java", null);
        Deck english = deckService.create("English", null);
        studySessionService.recordAnswers(java.getId(), StudyMode.QUIZ, 1, 2);
        studySessionService.recordAnswers(java.getId(), StudyMode.WRITE, 2, 2);
        studySessionService.recordAnswers(english.getId(), StudyMode.QUIZ, 0, 2);

        assertThat(studySessionService.findByDeck(java.getId()))
                .extracting(StudySession::getMode)
                .containsExactly(StudyMode.WRITE, StudyMode.QUIZ);
    }

    @Test
    void missingDeckIsReported() {
        assertThatThrownBy(() -> studySessionService.recordAnswers(999L, StudyMode.QUIZ, 1, 2))
                .isInstanceOf(NotFoundException.class);
    }
}
