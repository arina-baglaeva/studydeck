package com.studydeck.service;

import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Card;
import com.studydeck.model.Deck;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@Import({DeckService.class, CardService.class})
class CardServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private CardService cardService;

    @Test
    void addCardPutsCardIntoDeck() {
        Deck deck = deckService.create("English", null);

        cardService.addCard(deck.getId(), " bug ", "ошибка   в программе");

        assertThat(cardService.findByDeck(deck.getId()))
                .extracting(Card::getTerm, Card::getDefinition)
                .containsExactly(tuple("bug", "ошибка в программе"));
    }

    @Test
    void updateCardChangesText() {
        Deck deck = deckService.create("Java", null);
        Card card = cardService.addCard(deck.getId(), "JVM", "старое");

        cardService.updateCard(deck.getId(), card.getId(), "JVM", "Виртуальная машина Java");

        assertThat(cardService.getCard(deck.getId(), card.getId()).getDefinition())
                .isEqualTo("Виртуальная машина Java");
    }

    @Test
    void deleteCardRemovesOnlyThatCard() {
        Deck deck = deckService.create("Java", null);
        Card first = cardService.addCard(deck.getId(), "JDK", "набор разработчика");
        cardService.addCard(deck.getId(), "JRE", "среда выполнения");

        cardService.deleteCard(deck.getId(), first.getId());

        assertThat(cardService.findByDeck(deck.getId()))
                .extracting(Card::getTerm)
                .containsExactly("JRE");
    }

    @Test
    void cannotTouchCardThroughAnotherDeck() {
        Deck mine = deckService.create("Mine", null);
        Deck other = deckService.create("Other", null);
        Card card = cardService.addCard(mine.getId(), "term", "definition");

        assertThatThrownBy(() -> cardService.deleteCard(other.getId(), card.getId()))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> cardService.updateCard(other.getId(), card.getId(), "x", "y"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addCardToMissingDeckThrows() {
        assertThatThrownBy(() -> cardService.addCard(999L, "term", "definition"))
                .isInstanceOf(NotFoundException.class);
    }
}
