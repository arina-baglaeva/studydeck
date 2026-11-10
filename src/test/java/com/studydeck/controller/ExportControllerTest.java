package com.studydeck.controller;

import com.studydeck.exception.NotFoundException;
import com.studydeck.service.CardExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExportController.class)
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardExportService cardExportService;

    @Test
    void exportReturnsCsvFileWithBom() throws Exception {
        when(cardExportService.exportDeck(1L)).thenReturn("term,definition\r\n\"bug\",\"ошибка\"\r\n");
        byte[] expected = "﻿term,definition\r\n\"bug\",\"ошибка\"\r\n".getBytes(StandardCharsets.UTF_8);

        mockMvc.perform(get("/decks/1/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Disposition", containsString("deck-1.csv")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(content().bytes(expected));
    }

    @Test
    void exportOfMissingDeckReturns404() throws Exception {
        when(cardExportService.exportDeck(999L)).thenThrow(new NotFoundException("Колода не найдена"));

        mockMvc.perform(get("/decks/999/export"))
                .andExpect(status().isNotFound());
    }

    @Test
    void exportWithPostIsNotAllowed() throws Exception {
        mockMvc.perform(post("/decks/1/export"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void nonNumericDeckIdIsRejected() throws Exception {
        mockMvc.perform(get("/decks/abc/export"))
                .andExpect(status().is4xxClientError());
    }
}
