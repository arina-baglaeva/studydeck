package com.studydeck.controller;

import com.studydeck.dto.DeckSummary;
import com.studydeck.service.DeckService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeckService deckService;

    @Test
    void homeShowsDecksWithCardCount() throws Exception {
        when(deckService.findAllSummaries())
                .thenReturn(List.of(new DeckSummary(1L, "Java", "Термины", 3)));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("Java")))
                .andExpect(content().string(containsString("Карточек: 3")));
    }

    @Test
    void homeShowsMessageWhenNoDecks() throws Exception {
        when(deckService.findAllSummaries()).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Колод пока нет")));
    }

    @Test
    void deckTitleIsEscaped() throws Exception {
        when(deckService.findAllSummaries())
                .thenReturn(List.of(new DeckSummary(1L, "<script>alert(1)</script>", null, 0)));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }
}
