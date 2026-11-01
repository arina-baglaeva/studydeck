package com.studydeck.service;

import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Deck;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(DeckService.class)
class DeckServiceTest {

    @Autowired
    private DeckService deckService;

    @Test
    void createCleansExtraSpaces() {
        Deck deck = deckService.create("  Java   основы ", "   ");

        assertThat(deck.getTitle()).isEqualTo("Java основы");
        assertThat(deck.getDescription()).isNull();
    }

    @Test
    void updateChangesTitleAndDescription() {
        Deck deck = deckService.create("Old", null);

        deckService.update(deck.getId(), "New", "Описание");

        Deck updated = deckService.getById(deck.getId());
        assertThat(updated.getTitle()).isEqualTo("New");
        assertThat(updated.getDescription()).isEqualTo("Описание");
    }

    @Test
    void deleteRemovesDeck() {
        Deck deck = deckService.create("Temp", null);

        deckService.delete(deck.getId());

        assertThatThrownBy(() -> deckService.getById(deck.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getByIdThrowsWhenDeckMissing() {
        assertThatThrownBy(() -> deckService.getById(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
