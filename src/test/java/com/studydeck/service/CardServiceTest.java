package com.studydeck.service;

import com.studydeck.dto.ParsedCard;
import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Card;
import com.studydeck.model.Deck;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

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
    void addCardsSavesAllParsedCards() {
        Deck deck = deckService.create("Import", null);
        List<ParsedCard> parsed = List.of(new ParsedCard("bug", "ошибка"), new ParsedCard("fix", "исправление"));

        int added = cardService.addCards(deck.getId(), parsed);

        assertThat(added).isEqualTo(2);
        assertThat(cardService.findByDeck(deck.getId()))
                .extracting(Card::getTerm)
                .containsExactly("bug", "fix");
    }

    @Test
    void addCardsToMissingDeckThrows() {
        assertThatThrownBy(() -> cardService.addCards(999L, List.of(new ParsedCard("bug", "ошибка"))))
                .isInstanceOf(NotFoundException.class);
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

    @Test
    void searchFindsByTermAndDefinitionIgnoringCase() {
        Deck deck = deckService.create("Java", null);
        cardService.addCard(deck.getId(), "JVM", "виртуальная машина");
        cardService.addCard(deck.getId(), "JDK", "Набор для разработки на Java");
        cardService.addCard(deck.getId(), "bug", "ошибка");

        assertThat(cardService.search(deck.getId(), "jvm"))
                .extracting(Card::getTerm)
                .containsExactly("JVM");
        assertThat(cardService.search(deck.getId(), "  JAVA "))
                .extracting(Card::getTerm)
                .containsExactly("JDK");
        assertThat(cardService.search(deck.getId(), "МАШИНА"))
                .extracting(Card::getTerm)
                .containsExactly("JVM");
    }

    @Test
    void blankSearchReturnsAllCards() {
        Deck deck = deckService.create("Java", null);
        cardService.addCard(deck.getId(), "JVM", "машина");
        cardService.addCard(deck.getId(), "JDK", "набор");

        assertThat(cardService.search(deck.getId(), "   ")).hasSize(2);
        assertThat(cardService.search(deck.getId(), null)).hasSize(2);
    }

    @Test
    void searchTreatsWildcardsAsPlainText() {
        Deck deck = deckService.create("SQL", null);
        cardService.addCard(deck.getId(), "100%", "сто процентов");
        cardService.addCard(deck.getId(), "snake_case", "слова через подчёркивание");
        cardService.addCard(deck.getId(), "camelCase", "слова с большой буквы");

        assertThat(cardService.search(deck.getId(), "%"))
                .extracting(Card::getTerm)
                .containsExactly("100%");
        assertThat(cardService.search(deck.getId(), "_"))
                .extracting(Card::getTerm)
                .containsExactly("snake_case");
    }

    @Test
    void searchDoesNotReturnCardsFromOtherDecks() {
        Deck mine = deckService.create("Mine", null);
        Deck other = deckService.create("Other", null);
        cardService.addCard(other.getId(), "secret", "чужая карточка");

        assertThat(cardService.search(mine.getId(), "secret")).isEmpty();
    }

    @Test
    void searchInMissingDeckThrows() {
        assertThatThrownBy(() -> cardService.search(999L, "java"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void escapeLikeEscapesSpecialCharacters() {
        assertThat(CardService.escapeLike("50%_!")).isEqualTo("50!%!_!!");
    }
}
