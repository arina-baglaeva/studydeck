package com.studydeck.service;

import com.studydeck.dto.MatchGame;
import com.studydeck.dto.MatchItem;
import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Deck;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({DeckService.class, CardService.class, MatchGameService.class})
class MatchGameServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private CardService cardService;

    @Autowired
    private MatchGameService matchGameService;

    @Test
    void gameHasAtMostSixPairs() {
        Deck deck = createDeckWithCards(15);

        MatchGame game = matchGameService.newGame(deck.getId());

        assertThat(game.getPairCount()).isEqualTo(MatchGameService.MAX_PAIRS);
        assertThat(game.getDefinitions()).hasSize(MatchGameService.MAX_PAIRS);
    }

    @Test
    void smallDeckUsesAllCards() {
        Deck deck = createDeckWithCards(3);

        MatchGame game = matchGameService.newGame(deck.getId());

        assertThat(texts(game.getTerms())).containsExactlyInAnyOrder("term1", "term2", "term3");
        assertThat(game.isPlayable()).isTrue();
    }

    @Test
    void pairKeysConnectTermWithItsDefinition() {
        Deck deck = createDeckWithCards(6);

        MatchGame game = matchGameService.newGame(deck.getId());

        Map<Integer, String> definitionsByKey = game.getDefinitions().stream()
                .collect(Collectors.toMap(MatchItem::getPairKey, MatchItem::getText));
        for (MatchItem term : game.getTerms()) {
            String number = term.getText().substring("term".length());
            assertThat(definitionsByKey.get(term.getPairKey())).isEqualTo("definition" + number);
        }
    }

    @Test
    void pairKeysAreUnique() {
        Deck deck = createDeckWithCards(10);

        MatchGame game = matchGameService.newGame(deck.getId());

        Set<Integer> keys = game.getTerms().stream().map(MatchItem::getPairKey).collect(Collectors.toSet());
        assertThat(keys).hasSize(game.getPairCount());
    }

    @Test
    void cardsAreChosenRandomly() {
        Deck deck = createDeckWithCards(20);

        Set<Set<String>> differentGames = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            differentGames.add(new HashSet<>(texts(matchGameService.newGame(deck.getId()).getTerms())));
        }

        assertThat(differentGames.size()).isGreaterThan(1);
    }

    @Test
    void duplicateTermsAndDefinitionsAreSkipped() {
        Deck deck = deckService.create("Deck", null);
        cardService.addCard(deck.getId(), "JVM", "машина");
        cardService.addCard(deck.getId(), "jvm", "другое");
        cardService.addCard(deck.getId(), "JDK", "Машина");
        cardService.addCard(deck.getId(), "JRE", "среда");

        MatchGame game = matchGameService.newGame(deck.getId());

        Set<String> terms = lowerCase(texts(game.getTerms()));
        Set<String> definitions = lowerCase(texts(game.getDefinitions()));
        assertThat(terms).hasSize(game.getPairCount());
        assertThat(definitions).hasSize(game.getPairCount());
        assertThat(game.getPairCount()).isBetween(2, 3);
    }

    @Test
    void deckWithOneCardIsNotPlayable() {
        Deck deck = createDeckWithCards(1);

        assertThat(matchGameService.newGame(deck.getId()).isPlayable()).isFalse();
    }

    @Test
    void emptyDeckGivesEmptyGame() {
        Deck deck = deckService.create("Empty", null);

        MatchGame game = matchGameService.newGame(deck.getId());

        assertThat(game.getPairCount()).isZero();
        assertThat(game.isPlayable()).isFalse();
    }

    @Test
    void missingDeckThrowsNotFound() {
        assertThatThrownBy(() -> matchGameService.newGame(999L))
                .isInstanceOf(NotFoundException.class);
    }

    private Deck createDeckWithCards(int count) {
        Deck deck = deckService.create("Deck", null);
        for (int i = 1; i <= count; i++) {
            cardService.addCard(deck.getId(), "term" + i, "definition" + i);
        }
        return deck;
    }

    private List<String> texts(List<MatchItem> items) {
        return items.stream().map(MatchItem::getText).toList();
    }

    private Set<String> lowerCase(List<String> texts) {
        return texts.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }
}
