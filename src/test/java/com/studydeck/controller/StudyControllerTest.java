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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(StudyController.class)
class StudyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeckService deckService;

    @MockBean
    private CardService cardService;

    @Test
    void studyPageShowsTermsAndProgress() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardService.findByDeck(1L)).thenReturn(List.of(
                new Card("JVM", "виртуальная машина Java"),
                new Card("JDK", "набор для разработки")));

        mockMvc.perform(get("/decks/1/study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study"))
                .andExpect(content().string(containsString("JVM")))
                .andExpect(content().string(containsString("JDK")))
                .andExpect(content().string(containsString("study-position")))
                .andExpect(content().string(containsString("/js/study.js")));
    }

    @Test
    void onlyFirstCardIsVisibleAtStart() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardService.findByDeck(1L)).thenReturn(List.of(
                new Card("JVM", "машина"),
                new Card("JDK", "набор")));

        String html = mockMvc.perform(get("/decks/1/study"))
                .andReturn().getResponse().getContentAsString();

        int first = html.indexOf("class=\"study-card\"");
        int second = html.indexOf("class=\"study-card\"", first + 1);
        String firstTag = html.substring(first, html.indexOf('>', first));
        String secondTag = html.substring(second, html.indexOf('>', second));
        assertFalse(firstTag.contains("hidden"));
        assertTrue(secondTag.contains("hidden"));
    }

    @Test
    void cardHasFrontAndBackForFlipping() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardService.findByDeck(1L)).thenReturn(List.of(new Card("JVM", "машина")));

        mockMvc.perform(get("/decks/1/study"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"study-inner\"")))
                .andExpect(content().string(containsString("class=\"study-face study-front\"")))
                .andExpect(content().string(containsString("class=\"study-face study-back\" aria-hidden=\"true\"")))
                .andExpect(content().string(containsString("tabindex=\"0\"")))
                .andExpect(content().string(containsString("id=\"study-flip\"")));
    }

    @Test
    void emptyDeckShowsMessage() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardService.findByDeck(1L)).thenReturn(List.of());

        mockMvc.perform(get("/decks/1/study"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("В колоде нет карточек")))
                .andExpect(content().string(not(containsString("study-position"))));
    }

    @Test
    void cardTextIsEscaped() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardService.findByDeck(1L)).thenReturn(List.of(new Card("<b>x</b>", "<script>alert(1)</script>")));

        mockMvc.perform(get("/decks/1/study"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    void missingDeckReturns404() throws Exception {
        when(deckService.getById(999L)).thenThrow(new NotFoundException("Колода не найдена"));

        mockMvc.perform(get("/decks/999/study"))
                .andExpect(status().isNotFound());
    }

    @Test
    void nonNumericDeckIdReturns404() throws Exception {
        mockMvc.perform(get("/decks/abc/study"))
                .andExpect(status().isNotFound());
    }

    @Test
    void studyWithPostIsNotAllowed() throws Exception {
        mockMvc.perform(post("/decks/1/study"))
                .andExpect(status().isMethodNotAllowed());
    }
}
