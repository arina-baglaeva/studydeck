package com.studydeck.service;

import com.studydeck.dto.RecallResult;
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
import java.util.Random;

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

    @Test
    void dueCardsAreTakenFromRandomDeck() {
        Deck java = new Deck("Java", null);
        Card jvm = cardDueAt("JVM", NOW.minusDays(1));
        java.addCard(jvm);
        Deck english = new Deck("English", null);
        Card bug = cardDueAt("bug", NOW.minusDays(1));
        english.addCard(bug);
        deckRepository.save(java);
        deckRepository.save(english);

        assertThat(serviceWithRandom(0).nextCard(NOW)).contains(jvm);
        assertThat(serviceWithRandom(1).nextCard(NOW)).contains(bug);
    }

    @Test
    void newCardsAreTakenFromRandomDeck() {
        Deck java = new Deck("Java", null);
        Card jvm = new Card("JVM", "машина");
        java.addCard(jvm);
        java.addCard(new Card("JDK", "набор"));
        Deck english = new Deck("English", null);
        Card bug = new Card("bug", "ошибка");
        english.addCard(bug);
        deckRepository.save(java);
        deckRepository.save(english);

        assertThat(serviceWithRandom(0).nextCard(NOW)).contains(jvm);
        assertThat(serviceWithRandom(1).nextCard(NOW)).contains(bug);
    }

    @Test
    void dueCardFromOneDeckComesBeforeNewCardFromAnother() {
        Deck java = new Deck("Java", null);
        java.addCard(new Card("JVM", "машина"));
        Deck english = new Deck("English", null);
        Card bug = cardDueAt("bug", NOW.minusDays(1));
        english.addCard(bug);
        deckRepository.save(java);
        deckRepository.save(english);

        assertThat(serviceWithRandom(0).nextCard(NOW)).contains(bug);
    }

    @Test
    void oldestDueCardOfChosenDeckIsShown() {
        Deck java = new Deck("Java", null);
        java.addCard(cardDueAt("JVM", NOW.minusDays(1)));
        Card oldest = cardDueAt("JDK", NOW.minusDays(9));
        java.addCard(oldest);
        Deck english = new Deck("English", null);
        english.addCard(cardDueAt("bug", NOW.minusDays(3)));
        deckRepository.save(java);
        deckRepository.save(english);

        assertThat(serviceWithRandom(0).nextCard(NOW)).contains(oldest);
    }

    @Test
    void waitingCardIsFoundWithDeck() {
        Deck deck = new Deck("Java", null);
        Card card = new Card("JVM", "машина");
        deck.addCard(card);
        deckRepository.save(deck);

        Card found = reviewService.findWaitingCard(card.getId(), NOW).orElseThrow();

        assertThat(found.getDeck().getTitle()).isEqualTo("Java");
    }

    @Test
    void cardNotDueYetIsNotWaiting() {
        Deck deck = new Deck("Java", null);
        Card card = cardDueAt("JVM", NOW.plusDays(2));
        deck.addCard(card);
        deckRepository.save(deck);

        assertThat(reviewService.findWaitingCard(card.getId(), NOW)).isEmpty();
    }

    @Test
    void findingMissingCardThrowsNotFound() {
        assertThatThrownBy(() -> reviewService.findWaitingCard(999L, NOW))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void recallMatchesIgnoringCaseAndSpaces() {
        Card card = new Card("JVM", "Виртуальная машина Java");

        RecallResult result = reviewService.checkRecall(card, "  виртуальная   МАШИНА java ");

        assertThat(result.isTyped()).isTrue();
        assertThat(result.isMatches()).isTrue();
        assertThat(result.getTypedAnswer()).isEqualTo("виртуальная МАШИНА java");
    }

    @Test
    void differentRecallDoesNotMatch() {
        Card card = new Card("JVM", "Виртуальная машина Java");

        RecallResult result = reviewService.checkRecall(card, "компилятор");

        assertThat(result.isTyped()).isTrue();
        assertThat(result.isMatches()).isFalse();
    }

    @Test
    void emptyRecallMeansRememberedSilently() {
        Card card = new Card("JVM", "машина");

        assertThat(reviewService.checkRecall(card, null).isTyped()).isFalse();
        assertThat(reviewService.checkRecall(card, "   ").isTyped()).isFalse();
        assertThat(reviewService.checkRecall(card, "   ").isMatches()).isFalse();
    }

    @Test
    void tooLongRecallIsCut() {
        Card card = new Card("JVM", "машина");

        RecallResult result = reviewService.checkRecall(card, "а".repeat(Card.DEFINITION_MAX_LENGTH + 500));

        assertThat(result.getTypedAnswer()).hasSize(Card.DEFINITION_MAX_LENGTH);
    }

    @Test
    void defaultLimitIsTwentyNewCardsPerDay() {
        assertThat(reviewService.getNewCardsPerDay()).isEqualTo(20);
        assertThat(reviewService.newCardsLeftToday(NOW)).isEqualTo(20);
    }

    @Test
    void countShowsNoMoreThanTwentyNewCards() {
        deckWithNewCards(25);

        assertThat(reviewService.countForToday(NOW)).isEqualTo(20);
    }

    @Test
    void dueCardsAreNotLimited() {
        Deck deck = new Deck("Java", null);
        for (int i = 0; i < 30; i++) {
            deck.addCard(cardDueAt("term " + i, NOW.minusDays(1)));
        }
        deckRepository.save(deck);

        assertThat(serviceWithLimit(5).countForToday(NOW)).isEqualTo(30);
    }

    @Test
    void cardsStartedTodayUseUpTheLimit() {
        Deck deck = deckWithNewCards(5);
        deck.addCard(startedAt("JVM", NOW.minusHours(2)));
        deck.addCard(startedAt("JDK", NOW.minusHours(1)));
        deckRepository.save(deck);
        ReviewService service = serviceWithLimit(3);

        assertThat(service.newCardsLeftToday(NOW)).isEqualTo(1);
        assertThat(service.countForToday(NOW)).isEqualTo(1);
    }

    @Test
    void cardsStartedYesterdayDoNotCount() {
        Deck deck = deckWithNewCards(5);
        deck.addCard(startedAt("JVM", NOW.minusDays(1)));
        deckRepository.save(deck);

        assertThat(serviceWithLimit(3).newCardsLeftToday(NOW)).isEqualTo(3);
    }

    @Test
    void noNewCardIsShownWhenLimitIsReached() {
        Deck deck = deckWithNewCards(2);
        deck.addCard(startedAt("JVM", NOW.minusHours(1)));
        deckRepository.save(deck);
        ReviewService service = serviceWithLimit(1);

        assertThat(service.nextCard(NOW)).isEmpty();
        assertThat(service.countForToday(NOW)).isZero();
        assertThat(service.hasNewCardsWaitingForTomorrow(NOW)).isTrue();
    }

    @Test
    void dueCardIsStillShownWhenLimitIsReached() {
        Deck deck = new Deck("Java", null);
        deck.addCard(new Card("JDK", "набор"));
        Card due = cardDueAt("JRE", NOW.minusMinutes(1));
        due.setFirstReviewedAt(NOW.minusMinutes(20));
        deck.addCard(due);
        deckRepository.save(deck);

        assertThat(serviceWithLimit(1).nextCard(NOW)).contains(due);
    }

    @Test
    void gradingNewCardWhenLimitIsReachedIsIgnored() {
        Deck deck = deckWithNewCards(1);
        Card fresh = deck.getCards().get(0);
        deck.addCard(startedAt("JVM", NOW.minusHours(1)));
        deckRepository.save(deck);

        boolean graded = serviceWithLimit(1).grade(fresh.getId(), Grade.GOOD, NOW);

        assertThat(graded).isFalse();
        assertThat(cardRepository.findById(fresh.getId()).orElseThrow().isNew()).isTrue();
    }

    @Test
    void newCardOverLimitIsNotWaiting() {
        Deck deck = deckWithNewCards(1);
        Card fresh = deck.getCards().get(0);

        assertThat(serviceWithLimit(0).findWaitingCard(fresh.getId(), NOW)).isEmpty();
    }

    @Test
    void gradingNewCardUsesOnePlaceOfTheLimit() {
        Deck deck = deckWithNewCards(3);
        ReviewService service = serviceWithLimit(2);

        service.grade(deck.getCards().get(0).getId(), Grade.GOOD, NOW);

        assertThat(service.newCardsLeftToday(NOW)).isEqualTo(1);
        assertThat(service.countForToday(NOW)).isEqualTo(1);
    }

    @Test
    void wrongLimitIsRejected() {
        assertThatThrownBy(() -> serviceWithLimit(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> serviceWithLimit(ReviewService.MAX_NEW_CARDS_PER_DAY + 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private ReviewService serviceWithRandom(int pickedIndex) {
        Random random = new Random() {
            @Override
            public int nextInt(int bound) {
                return Math.min(pickedIndex, bound - 1);
            }
        };
        return new ReviewService(cardRepository, new SpacedRepetitionService(), random,
                ReviewService.DEFAULT_NEW_CARDS_PER_DAY);
    }

    private ReviewService serviceWithLimit(int newCardsPerDay) {
        return new ReviewService(cardRepository, new SpacedRepetitionService(), new Random(1), newCardsPerDay);
    }

    private Deck deckWithNewCards(int count) {
        Deck deck = new Deck("Java", null);
        for (int i = 0; i < count; i++) {
            deck.addCard(new Card("term " + i, "определение " + i));
        }
        return deckRepository.save(deck);
    }

    private Card startedAt(String term, LocalDateTime firstReviewedAt) {
        Card card = cardDueAt(term, NOW.plusDays(1));
        card.setFirstReviewedAt(firstReviewedAt);
        return card;
    }

    private Card cardDueAt(String term, LocalDateTime nextReviewAt) {
        Card card = new Card(term, "определение");
        card.setIntervalDays(1);
        card.setNextReviewAt(nextReviewAt);
        return card;
    }
}
