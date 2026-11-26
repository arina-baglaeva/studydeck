package com.studydeck.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudySessionTest {

    private final Deck deck = new Deck("Java", null);

    @Test
    void percentIsRoundedDown() {
        StudySession session = new StudySession(deck, StudyMode.QUIZ, 2, 3, null);

        assertThat(session.getPercent()).isEqualTo(66);
    }

    @Test
    void emptyResultHasZeroPercent() {
        StudySession session = new StudySession(deck, StudyMode.QUIZ, 0, 0, null);

        assertThat(session.getPercent()).isZero();
    }

    @Test
    void correctCountCannotBeBiggerThanTotal() {
        assertThatThrownBy(() -> new StudySession(deck, StudyMode.QUIZ, 5, 3, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativeCountsAreRejected() {
        assertThatThrownBy(() -> new StudySession(deck, StudyMode.WRITE, -1, 3, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StudySession(deck, StudyMode.WRITE, 0, -1, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativeDurationIsRejected() {
        assertThatThrownBy(() -> new StudySession(deck, StudyMode.MATCH, 4, 4, -5L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deckAndModeAreRequired() {
        assertThatThrownBy(() -> new StudySession(null, StudyMode.QUIZ, 1, 1, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StudySession(deck, null, 1, 1, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void modesHaveRussianLabels() {
        assertThat(StudyMode.QUIZ.getLabel()).isEqualTo("Тест");
        assertThat(StudyMode.WRITE.getLabel()).isEqualTo("Напиши ответ");
        assertThat(StudyMode.MATCH.getLabel()).isEqualTo("Соедини");
    }
}
