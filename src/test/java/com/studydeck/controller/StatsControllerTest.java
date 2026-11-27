package com.studydeck.controller;

import com.studydeck.dto.ModeStats;
import com.studydeck.model.Deck;
import com.studydeck.model.StudyMode;
import com.studydeck.model.StudySession;
import com.studydeck.service.StatsService;
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

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    @Test
    void statsPageShowsModesAndRecentSessions() throws Exception {
        when(statsService.totalSessions()).thenReturn(3L);
        when(statsService.modeStats()).thenReturn(List.of(
                new ModeStats(StudyMode.QUIZ, 2, 6, 8),
                ModeStats.empty(StudyMode.MATCH)));
        Deck deck = new Deck("Java", null);
        when(statsService.recentSessions()).thenReturn(List.of(
                new StudySession(deck, StudyMode.MATCH, 6, 6, 42_000L)));

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(view().name("stats"))
                .andExpect(content().string(containsString("Всего занятий: <strong>3</strong>")))
                .andExpect(content().string(containsString("6 из 8")))
                .andExpect(content().string(containsString("75%")))
                .andExpect(content().string(containsString("Соедини")))
                .andExpect(content().string(containsString("42 с")));
    }

    @Test
    void emptyStatsPageSaysNoSessions() throws Exception {
        when(statsService.modeStats()).thenReturn(List.of(ModeStats.empty(StudyMode.QUIZ)));

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Занятий пока не было")));
    }

    @Test
    void deckTitleIsEscaped() throws Exception {
        Deck deck = new Deck("<script>alert(1)</script>", null);
        when(statsService.recentSessions()).thenReturn(List.of(
                new StudySession(deck, StudyMode.QUIZ, 1, 2, null)));

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }
}
