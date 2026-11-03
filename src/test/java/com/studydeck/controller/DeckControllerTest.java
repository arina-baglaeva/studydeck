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
        when(cardService.findByDeck(1L)).thenReturn(List.of(new Card("JVM", "виртуальная машина Java")));

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
        when(cardService.findByDeck(1L)).thenReturn(List.of());

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
        when(cardService.findByDeck(1L)).thenReturn(List.of(new Card("<b>x</b>", "<script>alert(1)</script>")));

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
}
