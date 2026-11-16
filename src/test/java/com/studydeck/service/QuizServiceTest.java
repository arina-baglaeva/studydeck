package com.studydeck.service;

import com.studydeck.dto.QuizQuestion;
import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Deck;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({DeckService.class, CardService.class, QuizService.class})
class QuizServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private CardService cardService;

    @Autowired
    private QuizService quizService;

    @Test
    void everyQuestionHasFourDifferentOptions() {
        Deck deck = createDeckWithCards(6);

        List<QuizQuestion> questions = quizService.newQuiz(deck.getId());

        assertThat(questions).hasSize(6);
        for (QuizQuestion question : questions) {
            assertThat(question.getOptions()).hasSize(QuizService.OPTION_COUNT);
            assertThat(new HashSet<>(question.getOptions())).hasSize(QuizService.OPTION_COUNT);
        }
    }

    @Test
    void correctIndexPointsToDefinitionOfTerm() {
        Deck deck = createDeckWithCards(5);

        for (QuizQuestion question : quizService.newQuiz(deck.getId())) {
            String number = question.getTerm().substring("term".length());
            assertThat(question.getCorrectAnswer()).isEqualTo("definition" + number);
            assertThat(question.isCorrect(question.getCorrectIndex())).isTrue();
            assertThat(question.isCorrect(question.getCorrectIndex() + 1)).isFalse();
        }
    }

    @Test
    void quizHasAtMostTenQuestions() {
        Deck deck = createDeckWithCards(25);

        assertThat(quizService.newQuiz(deck.getId())).hasSize(QuizService.MAX_QUESTIONS);
    }

    @Test
    void deckWithThreeCardsGivesEmptyQuiz() {
        Deck deck = createDeckWithCards(3);

        assertThat(quizService.newQuiz(deck.getId())).isEmpty();
    }

    @Test
    void emptyDeckGivesEmptyQuiz() {
        Deck deck = deckService.create("Empty", null);

        assertThat(quizService.newQuiz(deck.getId())).isEmpty();
    }

    @Test
    void sameDefinitionIsNotUsedTwiceInOneQuestion() {
        Deck deck = deckService.create("Deck", null);
        cardService.addCard(deck.getId(), "JVM", "машина");
        cardService.addCard(deck.getId(), "JRE", "Машина");
        cardService.addCard(deck.getId(), "JDK", "набор");
        cardService.addCard(deck.getId(), "IDE", "среда");

        assertThat(quizService.newQuiz(deck.getId())).isEmpty();
    }

    @Test
    void duplicateTermIsAskedOnceAndNotUsedAsWrongOption() {
        Deck deck = createDeckWithCards(5);
        cardService.addCard(deck.getId(), "term1", "другое определение");

        List<QuizQuestion> questions = quizService.newQuiz(deck.getId());

        List<String> terms = questions.stream().map(QuizQuestion::getTerm).toList();
        assertThat(terms).containsOnlyOnce("term1");
        for (QuizQuestion question : questions) {
            if (question.getTerm().equals("term1")) {
                Set<String> options = new HashSet<>(question.getOptions());
                assertThat(options.contains("definition1") && options.contains("другое определение")).isFalse();
            }
        }
    }

    @Test
    void correctAnswerPositionChanges() {
        Deck deck = createDeckWithCards(10);

        Set<Integer> positions = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            positions.addAll(quizService.newQuiz(deck.getId()).stream()
                    .map(QuizQuestion::getCorrectIndex)
                    .collect(Collectors.toSet()));
        }

        assertThat(positions).hasSizeGreaterThan(1);
    }

    @Test
    void questionKeepsCardId() {
        Deck deck = createDeckWithCards(4);

        for (QuizQuestion question : quizService.newQuiz(deck.getId())) {
            assertThat(cardService.getCard(deck.getId(), question.getCardId()).getTerm())
                    .isEqualTo(question.getTerm());
        }
    }

    @Test
    void missingDeckThrowsNotFound() {
        assertThatThrownBy(() -> quizService.newQuiz(999L))
                .isInstanceOf(NotFoundException.class);
    }

    private Deck createDeckWithCards(int count) {
        Deck deck = deckService.create("Deck", null);
        for (int i = 1; i <= count; i++) {
            cardService.addCard(deck.getId(), "term" + i, "definition" + i);
        }
        return deck;
    }
}
