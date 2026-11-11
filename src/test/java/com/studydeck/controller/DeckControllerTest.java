package com.studydeck.controller;

import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Card;
import com.studydeck.model.Deck;
import com.studydeck.service.CardService;
import com.studydeck.service.DeckService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(DeckController.class)
class DeckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeckService deckService;

    @MockBean
    private CardService cardService;

    @Test
    void deckPageShowsCards() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", "Термины"));
        when(cardService.search(1L, null)).thenReturn(List.of(new Card("JVM", "виртуальная машина Java")));

        mockMvc.perform(get("/decks/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("deck"))
                .andExpect(content().string(containsString("JVM")))
                .andExpect(content().string(containsString("виртуальная машина Java")))
                .andExpect(content().string(containsString("Карточек: 1")));
    }

    @Test
    void deckPageShowsMessageWhenNoCards() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardService.search(1L, null)).thenReturn(List.of());

        mockMvc.perform(get("/decks/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("пока нет карточек")));
    }

    @Test
    void missingDeckReturns404() throws Exception {
        when(deckService.getById(999L)).thenThrow(new NotFoundException("Колода не найдена"));

        mockMvc.perform(get("/decks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cardTextIsEscaped() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardService.search(1L, null)).thenReturn(List.of(new Card("<b>x</b>", "<script>alert(1)</script>")));

        mockMvc.perform(get("/decks/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    void newDeckFormIsShown() throws Exception {
        mockMvc.perform(get("/decks/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("deck-form"))
                .andExpect(model().attributeExists("deckForm"));
    }

    @Test
    void validDeckIsCreatedAndRedirectsToDeckPage() throws Exception {
        Deck deck = mock(Deck.class);
        when(deck.getId()).thenReturn(5L);
        when(deckService.create("Java", "Термины")).thenReturn(deck);

        mockMvc.perform(post("/decks")
                        .param("title", "Java")
                        .param("description", "Термины"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/5"));

        verify(deckService).create("Java", "Термины");
    }

    @Test
    void blankTitleShowsError() throws Exception {
        mockMvc.perform(post("/decks")
                        .param("title", "   ")
                        .param("description", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("deck-form"))
                .andExpect(model().attributeHasFieldErrors("deckForm", "title"));

        verify(deckService, never()).create(any(), any());
    }

    @Test
    void tooLongTitleShowsError() throws Exception {
        mockMvc.perform(post("/decks")
                        .param("title", "a".repeat(101)))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("deckForm", "title"));

        verify(deckService, never()).create(any(), any());
    }

    @Test
    void tooLongDescriptionShowsError() throws Exception {
        mockMvc.perform(post("/decks")
                        .param("title", "Java")
                        .param("description", "a".repeat(501)))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("deckForm", "description"));

        verify(deckService, never()).create(any(), any());
    }

    @Test
    void deleteDeckRedirectsToHome() throws Exception {
        mockMvc.perform(post("/decks/3/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(deckService).delete(3L);
    }

    @Test
    void deleteMissingDeckReturns404() throws Exception {
        doThrow(new NotFoundException("Колода не найдена")).when(deckService).delete(999L);

        mockMvc.perform(post("/decks/999/delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDeckWithGetIsNotAllowed() throws Exception {
        mockMvc.perform(get("/decks/3/delete"))
                .andExpect(status().isMethodNotAllowed());

        verify(deckService, never()).delete(any());
    }

    @Test
    void deckPageHasDeleteForms() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardService.search(1L, null)).thenReturn(List.of(new Card("JVM", "машина")));

        mockMvc.perform(get("/decks/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/decks/1/delete\"")))
                .andExpect(content().string(containsString("Удалить эту карточку?")));
    }

    @Test
    void searchPassesQueryAndShowsFoundCount() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardService.search(1L, "jvm")).thenReturn(List.of(new Card("JVM", "машина")));

        mockMvc.perform(get("/decks/1").param("q", "jvm"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Найдено: 1")))
                .andExpect(content().string(containsString("value=\"jvm\"")));

        verify(cardService).search(1L, "jvm");
    }

    @Test
    void deckPageLinksToStudyMode() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardService.search(1L, null)).thenReturn(List.of(new Card("JVM", "машина")));

        mockMvc.perform(get("/decks/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/decks/1/study\"")));
    }

    @Test
    void searchWithoutResultsShowsMessage() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardService.search(1L, "xyz")).thenReturn(List.of());

        mockMvc.perform(get("/decks/1").param("q", "xyz"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("По запросу ничего не найдено")));
    }

    @Test
    void searchQueryIsEscaped() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardService.search(1L, "\"><script>alert(1)</script>")).thenReturn(List.of());

        mockMvc.perform(get("/decks/1").param("q", "\"><script>alert(1)</script>"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }

    @Test
    void missingDeckShowsFriendly404Page() throws Exception {
        when(deckService.getById(999L)).thenThrow(new NotFoundException("Колода не найдена"));

        mockMvc.perform(get("/decks/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(containsString("Колода не найдена")))
                .andExpect(content().string(containsString("К списку колод")));
    }

    @Test
    void notNumericDeckIdReturns404() throws Exception {
        mockMvc.perform(get("/decks/abc"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(not(containsString("NumberFormatException"))));
    }
}
