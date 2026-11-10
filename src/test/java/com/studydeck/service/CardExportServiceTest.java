package com.studydeck.service;

import com.studydeck.dto.ParseResult;
import com.studydeck.dto.ParsedCard;
import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Deck;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@Import({DeckService.class, CardService.class, CardExportService.class})
class CardExportServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private CardService cardService;

    @Autowired
    private CardExportService cardExportService;

    @Test
    void exportsHeaderAndCardsInOrder() {
        Deck deck = deckService.create("English", null);
        cardService.addCard(deck.getId(), "bug", "ошибка");
        cardService.addCard(deck.getId(), "fix", "исправление");

        String csv = cardExportService.exportDeck(deck.getId());

        assertThat(csv).isEqualTo("term,definition\r\n"
                + "\"bug\",\"ошибка\"\r\n"
                + "\"fix\",\"исправление\"\r\n");
    }

    @Test
    void emptyDeckExportsOnlyHeader() {
        Deck deck = deckService.create("Empty", null);

        assertThat(cardExportService.exportDeck(deck.getId())).isEqualTo("term,definition\r\n");
    }

    @Test
    void missingDeckThrowsNotFound() {
        assertThatThrownBy(() -> cardExportService.exportDeck(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void quotesAndCommasAreEscaped() {
        assertThat(CardExportService.toCsvCell("try, catch")).isEqualTo("\"try, catch\"");
        assertThat(CardExportService.toCsvCell("say \"hi\"")).isEqualTo("\"say \"\"hi\"\"\"");
    }

    @Test
    void formulaCellsGetApostrophePrefix() {
        assertThat(CardExportService.toCsvCell("=1+1")).isEqualTo("\"'=1+1\"");
        assertThat(CardExportService.toCsvCell("+7 999")).isEqualTo("\"'+7 999\"");
        assertThat(CardExportService.toCsvCell("-2")).isEqualTo("\"'-2\"");
        assertThat(CardExportService.toCsvCell("@SUM(A1)")).isEqualTo("\"'@SUM(A1)\"");
        assertThat(CardExportService.toCsvCell("\tcmd")).isEqualTo("\"'\tcmd\"");
    }

    @Test
    void safeCellsAreNotChanged() {
        assertThat(CardExportService.toCsvCell("a=b")).isEqualTo("\"a=b\"");
        assertThat(CardExportService.toCsvCell("e-mail")).isEqualTo("\"e-mail\"");
        assertThat(CardExportService.toCsvCell("")).isEqualTo("\"\"");
        assertThat(CardExportService.toCsvCell(null)).isEqualTo("\"\"");
    }

    @Test
    void exportedFileCanBeImportedBack() {
        Deck deck = deckService.create("Round trip", null);
        cardService.addCard(deck.getId(), "try, catch", "блок \"обработки\" ошибок");
        cardService.addCard(deck.getId(), "JVM", "машина; Java");

        String csv = cardExportService.exportDeck(deck.getId());
        ParseResult result = new CardFileParser().parseCsv(csv);

        assertThat(result.getCards())
                .extracting(ParsedCard::getTerm, ParsedCard::getDefinition)
                .containsExactly(
                        tuple("try, catch", "блок \"обработки\" ошибок"),
                        tuple("JVM", "машина; Java"));
        assertThat(result.getBadLines()).isEmpty();
    }
}
