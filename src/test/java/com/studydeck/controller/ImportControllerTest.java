package com.studydeck.controller;

import com.studydeck.dto.ParseResult;
import com.studydeck.dto.ParsedCard;
import com.studydeck.exception.ImportException;
import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Deck;
import com.studydeck.service.CardImportService;
import com.studydeck.service.CardService;
import com.studydeck.service.DeckService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ImportController.class)
class ImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeckService deckService;

    @MockBean
    private CardService cardService;

    @MockBean
    private CardImportService cardImportService;

    private final MockMultipartFile file =
            new MockMultipartFile("file", "words.txt", "text/plain", "bug - ошибка".getBytes());

    @Test
    void importFormIsShown() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));

        mockMvc.perform(get("/decks/1/import"))
                .andExpect(status().isOk())
                .andExpect(view().name("import"))
                .andExpect(content().string(containsString("Импорт карточек")))
                .andExpect(content().string(containsString("multipart/form-data")));
    }

    @Test
    void importFormForMissingDeckReturns404() throws Exception {
        when(deckService.getById(999L)).thenThrow(new NotFoundException("Колода не найдена"));

        mockMvc.perform(get("/decks/999/import"))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadedCardsAreSavedAndResultShown() throws Exception {
        ParseResult result = new ParseResult(List.of(new ParsedCard("bug", "ошибка")), List.of(3));
        when(cardImportService.readFile(any())).thenReturn(result);
        when(cardService.addCards(eq(1L), anyList())).thenReturn(1);

        mockMvc.perform(multipart("/decks/1/import").file(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/1/import"))
                .andExpect(flash().attribute("addedCount", 1))
                .andExpect(flash().attribute("badLines", List.of(3)));

        verify(cardService).addCards(1L, result.getCards());
    }

    @Test
    void importErrorIsShownOnForm() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardImportService.readFile(any())).thenThrow(new ImportException("Можно загрузить только файлы .txt или .csv"));

        mockMvc.perform(multipart("/decks/1/import").file(file))
                .andExpect(status().isOk())
                .andExpect(view().name("import"))
                .andExpect(model().attribute("importError", "Можно загрузить только файлы .txt или .csv"));

        verify(cardService, never()).addCards(any(), anyList());
    }

    @Test
    void uploadToMissingDeckReturns404() throws Exception {
        when(deckService.getById(999L)).thenThrow(new NotFoundException("Колода не найдена"));

        mockMvc.perform(multipart("/decks/999/import").file(file))
                .andExpect(status().isNotFound());

        verify(cardImportService, never()).readFile(any());
    }
}
