package com.studydeck.service;

import com.studydeck.dto.ParseResult;
import com.studydeck.dto.ParsedCard;
import com.studydeck.exception.ImportException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardImportServiceTest {

    private final CardImportService service = new CardImportService(new CardFileParser());

    private MockMultipartFile file(String name, String text) {
        return new MockMultipartFile("file", name, "text/plain", text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void readsTxtFile() {
        ParseResult result = service.readFile(file("words.txt", "bug - ошибка\nfix - исправление"));

        assertThat(result.getCards()).extracting(ParsedCard::getTerm).containsExactly("bug", "fix");
    }

    @Test
    void readsCsvFileWithBom() {
        ParseResult result = service.readFile(file("words.csv", "﻿term,definition\nbug,ошибка"));

        assertThat(result.getCards()).extracting(ParsedCard::getTerm).containsExactly("bug");
    }

    @Test
    void rejectsEmptyFile() {
        assertThatThrownBy(() -> service.readFile(file("words.txt", "")))
                .isInstanceOf(ImportException.class);
        assertThatThrownBy(() -> service.readFile(null))
                .isInstanceOf(ImportException.class);
    }

    @Test
    void rejectsOtherExtensions() {
        assertThatThrownBy(() -> service.readFile(file("script.html", "bug - ошибка")))
                .isInstanceOf(ImportException.class)
                .hasMessageContaining(".txt");
        assertThatThrownBy(() -> service.readFile(file("words.txt.exe", "bug - ошибка")))
                .isInstanceOf(ImportException.class);
    }

    @Test
    void rejectsTooLargeFile() {
        String text = "a".repeat((int) CardImportService.MAX_FILE_SIZE + 1);

        assertThatThrownBy(() -> service.readFile(file("big.txt", text)))
                .isInstanceOf(ImportException.class)
                .hasMessageContaining("1 МБ");
    }

    @Test
    void rejectsTooManyLines() {
        String text = "bug - ошибка\n".repeat(CardImportService.MAX_LINES + 1);

        assertThatThrownBy(() -> service.readFile(file("many.txt", text)))
                .isInstanceOf(ImportException.class)
                .hasMessageContaining("строк");
    }

    @Test
    void rejectsNotUtf8File() {
        byte[] bytes = {(byte) 0xC3, (byte) 0x28, 0x20, 0x2D, 0x20, 0x61};
        MockMultipartFile file = new MockMultipartFile("file", "bad.txt", "text/plain", bytes);

        assertThatThrownBy(() -> service.readFile(file))
                .isInstanceOf(ImportException.class)
                .hasMessageContaining("UTF-8");
    }

    @Test
    void tooLongTermIsReportedAsBadLine() {
        String longTerm = "x".repeat(com.studydeck.model.Card.TERM_MAX_LENGTH + 1);

        ParseResult result = service.readFile(file("words.txt", "bug - ошибка\n" + longTerm + " - слишком длинно"));

        assertThat(result.getCards()).hasSize(1);
        assertThat(result.getBadLines()).containsExactly(2);
    }

    @Test
    void rejectsBinaryFileWithTxtExtension() {
        byte[] bytes = {0x50, 0x4B, 0x03, 0x04, 0x00, 0x00, 0x20, 0x2D, 0x20, 0x61};
        MockMultipartFile file = new MockMultipartFile("file", "archive.txt", "text/plain", bytes);

        assertThatThrownBy(() -> service.readFile(file))
                .isInstanceOf(ImportException.class)
                .hasMessageContaining("двоичный");
    }

    @Test
    void tabsAndLineBreaksAreNotBinary() {
        assertThat(service.looksBinary("bug\tterm\r\nfix\tdefinition")).isFalse();
        assertThat(service.looksBinary("bug\u0007 - bell")).isTrue();
    }

    @Test
    void uppercaseExtensionIsAllowed() {
        assertThat(service.hasAllowedExtension("WORDS.TXT")).isTrue();
        assertThat(service.hasAllowedExtension("words.CSV")).isTrue();
        assertThat(service.hasAllowedExtension("words")).isFalse();
        assertThat(service.hasAllowedExtension(null)).isFalse();
    }
}
