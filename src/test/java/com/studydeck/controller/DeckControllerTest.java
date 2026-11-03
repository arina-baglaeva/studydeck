package com.studydeck.controller;

import com.studydeck.service.DeckService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void newDeckFormIsShown() throws Exception {
        mockMvc.perform(get("/decks/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("deck-form"))
                .andExpect(model().attributeExists("deckForm"));
    }

    @Test
    void validDeckIsCreatedAndRedirects() throws Exception {
        mockMvc.perform(post("/decks")
                        .param("title", "Java")
                        .param("description", "Термины"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

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
