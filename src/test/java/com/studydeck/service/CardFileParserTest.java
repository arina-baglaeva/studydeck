package com.studydeck.service;

import com.studydeck.dto.ParseResult;
import com.studydeck.dto.ParsedCard;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class CardFileParserTest {

    private final CardFileParser parser = new CardFileParser();

    @Test
    void parsesTermAndDefinition() {
        ParseResult result = parser.parse("bug - ошибка\nfeature - новая возможность");

        assertThat(result.getCards())
                .extracting(ParsedCard::getTerm, ParsedCard::getDefinition)
                .containsExactly(
                        tuple("bug", "ошибка"),
                        tuple("feature", "новая возможность"));
        assertThat(result.getBadLines()).isEmpty();
    }

    @Test
    void trimsSpacesAroundTermAndDefinition() {
        ParseResult result = parser.parse("   JVM   -   виртуальная    машина  ");

        assertThat(result.getCards())
                .extracting(ParsedCard::getTerm, ParsedCard::getDefinition)
                .containsExactly(tuple("JVM", "виртуальная машина"));
    }

    @Test
    void splitsOnlyOnFirstDelimiter() {
        ParseResult result = parser.parse("try - catch - обработка ошибок");

        assertThat(result.getCards())
                .extracting(ParsedCard::getTerm, ParsedCard::getDefinition)
                .containsExactly(tuple("try", "catch - обработка ошибок"));
    }

    @Test
    void skipsBlankLines() {
        ParseResult result = parser.parse("\n\nbug - ошибка\n   \n\r\nfix - исправление\n");

        assertThat(result.getCards()).hasSize(2);
        assertThat(result.getBadLines()).isEmpty();
    }

    @Test
    void reportsLinesWithoutDelimiter() {
        ParseResult result = parser.parse("bug - ошибка\nпросто текст\n - нет термина\nнет определения - ");

        assertThat(result.getCards()).hasSize(1);
        assertThat(result.getBadLines()).containsExactly(2, 3, 4);
    }

    @Test
    void handlesWindowsLineEndings() {
        ParseResult result = parser.parse("bug - ошибка\r\nfix - исправление\r\n");

        assertThat(result.getCards())
                .extracting(ParsedCard::getDefinition)
                .containsExactly("ошибка", "исправление");
    }

    @Test
    void nullOrEmptyTextGivesEmptyResult() {
        assertThat(parser.parse(null).getCards()).isEmpty();
        assertThat(parser.parse("").getCards()).isEmpty();
        assertThat(parser.parse("").getBadLines()).isEmpty();
    }
}
