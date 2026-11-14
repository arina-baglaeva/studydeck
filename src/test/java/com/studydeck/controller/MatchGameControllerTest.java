package com.studydeck.controller;

import com.studydeck.dto.MatchGame;
import com.studydeck.dto.MatchItem;
import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Deck;
import com.studydeck.service.DeckService;
import com.studydeck.service.MatchGameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(MatchGameController.class)
class MatchGameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeckService deckService;

    @MockBean
    private MatchGameService matchGameService;

    @Test
    void matchPageShowsTwoColumns() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(matchGameService.newGame(1L)).thenReturn(new MatchGame(
                List.of(new MatchItem(1, "JVM"), new MatchItem(2, "JDK")),
                List.of(new MatchItem(2, "набор"), new MatchItem(1, "машина"))));

        mockMvc.perform(get("/decks/1/match"))
                .andExpect(status().isOk())
                .andExpect(view().name("match"))
                .andExpect(content().string(containsString("Термины")))
                .andExpect(content().string(containsString("Определения")))
                .andExpect(content().string(containsString("JVM")))
                .andExpect(content().string(containsString("машина")))
                .andExpect(content().string(containsString("data-pair=\"1\"")))
                .andExpect(content().string(containsString("Пар в игре: <span>2</span>")));
    }

    @Test
    void notPlayableGameShowsMessage() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(matchGameService.newGame(1L)).thenReturn(new MatchGame(
                List.of(new MatchItem(1, "JVM")), List.of(new MatchItem(1, "машина"))));

        mockMvc.perform(get("/decks/1/match"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Для игры нужно хотя бы")))
                .andExpect(content().string(not(containsString("match-board"))));
    }

    @Test
    void cardTextIsEscaped() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(matchGameService.newGame(1L)).thenReturn(new MatchGame(
                List.of(new MatchItem(1, "<b>x</b>"), new MatchItem(2, "y")),
                List.of(new MatchItem(1, "<script>alert(1)</script>"), new MatchItem(2, "z"))));

        mockMvc.perform(get("/decks/1/match"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    void missingDeckReturns404() throws Exception {
        when(deckService.getById(999L)).thenThrow(new NotFoundException("Колода не найдена"));

        mockMvc.perform(get("/decks/999/match"))
                .andExpect(status().isNotFound());
    }

    @Test
    void nonNumericDeckIdReturns404() throws Exception {
        mockMvc.perform(get("/decks/abc/match"))
                .andExpect(status().isNotFound());
    }

    @Test
    void matchWithPostIsNotAllowed() throws Exception {
        mockMvc.perform(post("/decks/1/match"))
                .andExpect(status().isMethodNotAllowed());
    }
}
