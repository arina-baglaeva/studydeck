package com.studydeck.service;

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

@DataJpaTest
@Import({DeckService.class, CardService.class, StudyService.class})
class StudyServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private CardService cardService;

    @Autowired
    private StudyService studyService;

    @Test
    void withoutShuffleCardsKeepDeckOrder() {
        Deck deck = createDeckWithCards(5);

        List<String> terms = terms(studyService.cardsForStudy(deck.getId(), false));

        assertThat(terms).containsExactly("term1", "term2", "term3", "term4", "term5");
    }

    @Test
    void shuffleKeepsTheSameCards() {
        Deck deck = createDeckWithCards(10);

        List<String> terms = terms(studyService.cardsForStudy(deck.getId(), true));

        assertThat(terms).containsExactlyInAnyOrderElementsOf(terms(cardService.findByDeck(deck.getId())));
    }

    @Test
    void shuffleChangesOrder() {
        Deck deck = createDeckWithCards(20);
        List<String> original = terms(cardService.findByDeck(deck.getId()));

        List<String> shuffled = terms(studyService.cardsForStudy(deck.getId(), true));

        assertThat(shuffled).isNotEqualTo(original);
    }

    @Test
    void emptyDeckGivesEmptyList() {
        Deck deck = deckService.create("Empty", null);

        assertThat(studyService.cardsForStudy(deck.getId(), true)).isEmpty();
    }

    @Test
    void missingDeckThrowsNotFound() {
        assertThatThrownBy(() -> studyService.cardsForStudy(999L, true))
                .isInstanceOf(NotFoundException.class);
    }

    private Deck createDeckWithCards(int count) {
        Deck deck = deckService.create("Deck", null);
        for (int i = 1; i <= count; i++) {
            cardService.addCard(deck.getId(), "term" + i, "definition" + i);
        }
        return deck;
    }

    private List<String> terms(List<Card> cards) {
        return cards.stream().map(Card::getTerm).toList();
    }
}
