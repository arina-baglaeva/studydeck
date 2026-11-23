package com.studydeck.service;

import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Card;
import com.studydeck.model.Deck;
import com.studydeck.model.Grade;
import com.studydeck.repository.CardRepository;
import com.studydeck.repository.DeckRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({ReviewService.class, SpacedRepetitionService.class})
class ReviewServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 23, 12, 0);

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private CardRepository cardRepository;

    @Test
    void nothingToReviewWhenThereAreNoCards() {
        assertThat(reviewService.nextCard(NOW)).isEmpty();
        assertThat(reviewService.countForToday(NOW)).isZero();
    }

    @Test
    void dueCardsComeBeforeNewCards() {
        Deck deck = new Deck("Java", null);
        Card newCard = new Card("JDK", "набор");
        Card dueCard = cardDueAt("JVM", NOW.minusDays(1));
        deck.addCard(newCard);
        deck.addCard(dueCard);
        deckRepository.save(deck);

        assertThat(reviewService.nextCard(NOW)).contains(dueCard);
    }

    @Test
    void oldestDueCardIsShownFirst() {
        Deck deck = new Deck("Java", null);
        Card yesterday = cardDueAt("JVM", NOW.minusDays(1));
        Card lastWeek = cardDueAt("JDK", NOW.minusDays(7));
        deck.addCard(yesterday);
        deck.addCard(lastWeek);
        deckRepository.save(deck);

        assertThat(reviewService.nextCard(NOW)).contains(lastWeek);
    }

    @Test
    void newCardIsShownWhenNothingIsDue() {
        Deck deck = new Deck("Java", null);
        Card newCard = new Card("JDK", "набор");
        deck.addCard(cardDueAt("JVM", NOW.plusDays(3)));
        deck.addCard(newCard);
        deckRepository.save(deck);

        assertThat(reviewService.nextCard(NOW)).contains(newCard);
    }

    @Test
    void countIncludesDueAndNewCardsButNotFutureOnes() {
        Deck deck = new Deck("Java", null);
        deck.addCard(new Card("JDK", "набор"));
        deck.addCard(cardDueAt("JVM", NOW.minusMinutes(5)));
        deck.addCard(cardDueAt("JRE", NOW));
        deck.addCard(cardDueAt("JIT", NOW.plusDays(2)));
        deckRepository.save(deck);

        assertThat(reviewService.countForToday(NOW)).isEqualTo(3);
    }

    @Test
    void cardsFromDifferentDecksAreCounted() {
        Deck java = new Deck("Java", null);
        java.addCard(new Card("JVM", "машина"));
        Deck english = new Deck("English", null);
        english.addCard(new Card("bug", "ошибка"));
        deckRepository.save(java);
        deckRepository.save(english);

        assertThat(reviewService.countForToday(NOW)).isEqualTo(2);
    }

    @Test
    void gradingNewCardSchedulesIt() {
        Deck deck = new Deck("Java", null);
        Card card = new Card("JVM", "машина");
        deck.addCard(card);
        deckRepository.save(deck);

        boolean graded = reviewService.grade(card.getId(), Grade.AGAIN, NOW);

        assertThat(graded).isTrue();
        Card loaded = cardRepository.findById(card.getId()).orElseThrow();
        assertThat(loaded.getNextReviewAt()).isEqualTo(NOW.plusMinutes(SpacedRepetitionService.LEARNING_STEP_MINUTES));
        assertThat(reviewService.countForToday(NOW)).isZero();
    }

    @Test
    void cardNotDueYetIsNotChanged() {
        Deck deck = new Deck("Java", null);
        Card card = cardDueAt("JVM", NOW.plusDays(5));
        card.setIntervalDays(10);
        deck.addCard(card);
        deckRepository.save(deck);

        boolean graded = reviewService.grade(card.getId(), Grade.EASY, NOW);

        assertThat(graded).isFalse();
        Card loaded = cardRepository.findById(card.getId()).orElseThrow();
        assertThat(loaded.getIntervalDays()).isEqualTo(10);
        assertThat(loaded.getNextReviewAt()).isEqualTo(NOW.plusDays(5));
    }

    @Test
    void gradingMissingCardThrowsNotFound() {
        assertThatThrownBy(() -> reviewService.grade(999L, Grade.GOOD, NOW))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void nextCardHasDeckLoaded() {
        Deck deck = new Deck("Java", null);
        deck.addCard(new Card("JVM", "машина"));
        deckRepository.save(deck);

        Card card = reviewService.nextCard(NOW).orElseThrow();

        assertThat(card.getDeck().getTitle()).isEqualTo("Java");
    }

    private Card cardDueAt(String term, LocalDateTime nextReviewAt) {
        Card card = new Card(term, "определение");
        card.setIntervalDays(1);
        card.setNextReviewAt(nextReviewAt);
        return card;
    }
}
