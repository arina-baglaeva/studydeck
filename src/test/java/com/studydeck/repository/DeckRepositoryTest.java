package com.studydeck.repository;

import com.studydeck.model.Card;
import com.studydeck.model.Deck;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DeckRepositoryTest {

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private CardRepository cardRepository;

    @Test
    void savesDeckWithCards() {
        Deck deck = new Deck("Java", "Основные термины");
        deck.addCard(new Card("JVM", "Виртуальная машина Java"));
        deck.addCard(new Card("JDK", "Набор инструментов разработчика"));

        Deck saved = deckRepository.save(deck);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(cardRepository.countByDeckId(saved.getId())).isEqualTo(2);
    }

    @Test
    void deletingDeckDeletesItsCards() {
        Deck deck = new Deck("English", null);
        deck.addCard(new Card("bug", "ошибка в программе"));
        Deck saved = deckRepository.save(deck);

        deckRepository.delete(saved);
        deckRepository.flush();

        assertThat(cardRepository.count()).isZero();
    }

    @Test
    void findsCardOnlyInsideItsDeck() {
        Deck first = new Deck("First", null);
        Card card = new Card("term", "definition");
        first.addCard(card);
        deckRepository.save(first);
        Deck second = deckRepository.save(new Deck("Second", null));

        assertThat(cardRepository.findByIdAndDeckId(card.getId(), first.getId())).isPresent();
        assertThat(cardRepository.findByIdAndDeckId(card.getId(), second.getId())).isEmpty();
    }
}
