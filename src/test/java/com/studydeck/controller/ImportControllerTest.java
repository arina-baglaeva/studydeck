package com.studydeck.controller;

import com.studydeck.dto.ImportPreview;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private final ParsedCard bug = new ParsedCard("bug", "ошибка");
    private final ParsedCard fix = new ParsedCard("fix", "исправление");

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
    void uploadedFileIsShownAsPreviewBeforeSaving() throws Exception {
        List<ParsedCard> cards = List.of(new ParsedCard("bug", "ошибка"), new ParsedCard("Bug", "жук"));
        ParseResult result = new ParseResult(cards, List.of(3));
        when(cardImportService.readFile(any())).thenReturn(result);
        when(cardService.withoutDuplicates(1L, cards)).thenReturn(List.of(cards.get(0)));
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(multipart("/decks/1/import").file(file).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/1/import/preview"));

        ImportPreview preview = (ImportPreview) session.getAttribute(ImportController.PREVIEW_ATTRIBUTE);
        assertThat(preview.getCards()).extracting(ParsedCard::getTerm).containsExactly("bug");
        assertThat(preview.getDuplicateCount()).isEqualTo(1);
        assertThat(preview.getBadLines()).containsExactly(3);
        verify(cardService, never()).addCards(any(), anyList());
    }

    @Test
    void previewPageShowsCards() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));

        mockMvc.perform(get("/decks/1/import/preview").session(sessionWithPreview(1L)))
                .andExpect(status().isOk())
                .andExpect(view().name("import-preview"))
                .andExpect(content().string(containsString("исправление")))
                .andExpect(content().string(containsString("Пропущено дубликатов: 2")));
    }

    @Test
    void previewWithoutUploadRedirectsToForm() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));

        mockMvc.perform(get("/decks/1/import/preview"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/1/import"));
    }

    @Test
    void confirmSavesOnlySelectedCards() throws Exception {
        MockHttpSession session = sessionWithPreview(1L);
        when(cardService.addCards(eq(1L), anyList())).thenReturn(1);

        mockMvc.perform(post("/decks/1/import/confirm").session(session).param("selected", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/1/import"))
                .andExpect(flash().attribute("addedCount", 1));

        verify(cardService).addCards(1L, List.of(fix));
        assertThat(session.getAttribute(ImportController.PREVIEW_ATTRIBUTE)).isNull();
    }

    @Test
    void confirmWithNothingSelectedReturnsToPreview() throws Exception {
        mockMvc.perform(post("/decks/1/import/confirm").session(sessionWithPreview(1L)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/1/import/preview"));

        verify(cardService, never()).addCards(any(), anyList());
    }

    @Test
    void previewOfAnotherDeckCannotBeSaved() throws Exception {
        mockMvc.perform(post("/decks/2/import/confirm").session(sessionWithPreview(1L)).param("selected", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/2/import"));

        verify(cardService, never()).addCards(any(), anyList());
    }

    @Test
    void cancelRemovesPreview() throws Exception {
        MockHttpSession session = sessionWithPreview(1L);

        mockMvc.perform(post("/decks/1/import/cancel").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/1"));

        assertThat(session.getAttribute(ImportController.PREVIEW_ATTRIBUTE)).isNull();
    }

    private MockHttpSession sessionWithPreview(Long deckId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(ImportController.PREVIEW_ATTRIBUTE,
                new ImportPreview(deckId, List.of(bug, fix), List.of(), 2));
        return session;
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

    @Test
    void tooLargeUploadShowsFriendlyMessage() throws Exception {
        when(cardImportService.readFile(any())).thenThrow(new MaxUploadSizeExceededException(2L * 1024 * 1024));

        mockMvc.perform(multipart("/decks/1/import").file(file))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(view().name("error/4xx"))
                .andExpect(content().string(containsString("Файл слишком большой")));
    }
}
