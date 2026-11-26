package com.studydeck.controller;

import com.studydeck.dto.MatchGame;
import com.studydeck.dto.MatchItem;
import com.studydeck.dto.MatchResult;
import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Deck;
import com.studydeck.service.DeckService;
import com.studydeck.service.MatchGameService;
import com.studydeck.service.StudySessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(MatchGameController.class)
class MatchGameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudySessionService studySessionService;

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
    void matchPageIncludesGameScriptAndStatus() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(matchGameService.newGame(1L)).thenReturn(new MatchGame(
                List.of(new MatchItem(1, "JVM"), new MatchItem(2, "JDK")),
                List.of(new MatchItem(2, "набор"), new MatchItem(1, "машина"))));

        mockMvc.perform(get("/decks/1/match"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/js/match.js")))
                .andExpect(content().string(containsString("data-side=\"term\"")))
                .andExpect(content().string(containsString("data-side=\"definition\"")))
                .andExpect(content().string(containsString("id=\"match-found\"")))
                .andExpect(content().string(containsString("id=\"match-mistakes\"")))
                .andExpect(content().string(containsString("id=\"match-feedback\"")))
                .andExpect(content().string(containsString("id=\"match-timer\"")))
                .andExpect(content().string(containsString("id=\"match-final-time\"")));
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
    void matchPageRemembersStartTimeInSession() throws Exception {
        givenPlayableGame();
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/decks/1/match").session(session))
                .andExpect(status().isOk());

        assertThat(session.getAttribute(MatchGameController.startedAtKey(1L))).isInstanceOf(Instant.class);
    }

    @Test
    void matchPageShowsNoBestTimeYet() throws Exception {
        givenPlayableGame();

        mockMvc.perform(get("/decks/1/match"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Лучшее время")))
                .andExpect(content().string(containsString("ещё нет")))
                .andExpect(content().string(containsString("data-result-url=\"/decks/1/match/result\"")));
    }

    @Test
    void matchPageShowsBestTime() throws Exception {
        Deck deck = new Deck("Java", null);
        deck.recordMatchTime(83_000);
        when(deckService.getById(1L)).thenReturn(deck);
        when(matchGameService.newGame(1L)).thenReturn(playableGame());

        mockMvc.perform(get("/decks/1/match"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("01:23")));
    }

    @Test
    void resultIsSavedAfterGameStarted() throws Exception {
        MockHttpSession session = startedGameSession(1L);
        when(matchGameService.isAcceptableTime(eq(20_000L), anyLong())).thenReturn(true);
        when(deckService.recordMatchTime(1L, 20_000)).thenReturn(new MatchResult(20_000, true));

        mockMvc.perform(post("/decks/1/match/result").session(session).param("timeMillis", "20000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bestTimeMillis").value(20000))
                .andExpect(jsonPath("$.bestTime").value("00:20"))
                .andExpect(jsonPath("$.newRecord").value(true));
    }

    @Test
    void resultCanBeSavedOnlyOncePerGame() throws Exception {
        MockHttpSession session = startedGameSession(1L);
        when(matchGameService.isAcceptableTime(eq(20_000L), anyLong())).thenReturn(true);
        when(deckService.recordMatchTime(1L, 20_000)).thenReturn(new MatchResult(20_000, true));

        mockMvc.perform(post("/decks/1/match/result").session(session).param("timeMillis", "20000"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/decks/1/match/result").session(session).param("timeMillis", "20000"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resultWithoutStartedGameIsRejected() throws Exception {
        mockMvc.perform(post("/decks/1/match/result").param("timeMillis", "20000"))
                .andExpect(status().isBadRequest());

        verify(deckService, never()).recordMatchTime(anyLong(), anyLong());
    }

    @Test
    void resultForOtherDeckIsRejected() throws Exception {
        MockHttpSession session = startedGameSession(1L);

        mockMvc.perform(post("/decks/2/match/result").session(session).param("timeMillis", "20000"))
                .andExpect(status().isBadRequest());

        verify(deckService, never()).recordMatchTime(anyLong(), anyLong());
    }

    @Test
    void unacceptableTimeIsRejected() throws Exception {
        MockHttpSession session = startedGameSession(1L);
        when(matchGameService.isAcceptableTime(eq(10L), anyLong())).thenReturn(false);

        mockMvc.perform(post("/decks/1/match/result").session(session).param("timeMillis", "10"))
                .andExpect(status().isBadRequest());

        verify(deckService, never()).recordMatchTime(anyLong(), anyLong());
    }

    @Test
    void resultWithoutTimeIsRejected() throws Exception {
        mockMvc.perform(post("/decks/1/match/result").session(startedGameSession(1L)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resultForMissingDeckReturns404() throws Exception {
        MockHttpSession session = startedGameSession(999L);
        when(matchGameService.isAcceptableTime(eq(20_000L), anyLong())).thenReturn(true);
        when(deckService.recordMatchTime(999L, 20_000)).thenThrow(new NotFoundException("Колода не найдена"));

        mockMvc.perform(post("/decks/999/match/result").session(session).param("timeMillis", "20000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void resultWithGetIsNotAllowed() throws Exception {
        mockMvc.perform(get("/decks/1/match/result"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void matchPageRemembersPairCountInSession() throws Exception {
        givenPlayableGame();
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/decks/1/match").session(session))
                .andExpect(status().isOk());

        assertThat(session.getAttribute(MatchGameController.pairsKey(1L))).isEqualTo(2);
    }

    @Test
    void savedResultIsRecordedAsStudySession() throws Exception {
        MockHttpSession session = startedGameSession(1L);
        when(matchGameService.isAcceptableTime(eq(20_000L), anyLong())).thenReturn(true);
        when(deckService.recordMatchTime(1L, 20_000)).thenReturn(new MatchResult(20_000, true));

        mockMvc.perform(post("/decks/1/match/result").session(session).param("timeMillis", "20000"))
                .andExpect(status().isOk());

        verify(studySessionService).recordMatch(1L, 6, 20_000);
    }

    @Test
    void resultWithoutPairCountIsRejected() throws Exception {
        MockHttpSession session = startedGameSession(1L);
        session.removeAttribute(MatchGameController.pairsKey(1L));

        mockMvc.perform(post("/decks/1/match/result").session(session).param("timeMillis", "20000"))
                .andExpect(status().isBadRequest());

        verify(studySessionService, never()).recordMatch(anyLong(), anyInt(), anyLong());
    }

    @Test
    void resultOfNotPlayableGameIsRejected() throws Exception {
        MockHttpSession session = startedGameSession(1L);
        session.setAttribute(MatchGameController.pairsKey(1L), 1);

        mockMvc.perform(post("/decks/1/match/result").session(session).param("timeMillis", "20000"))
                .andExpect(status().isBadRequest());

        verify(deckService, never()).recordMatchTime(anyLong(), anyLong());
        verify(studySessionService, never()).recordMatch(anyLong(), anyInt(), anyLong());
    }

    @Test
    void rejectedTimeIsNotRecordedAsStudySession() throws Exception {
        MockHttpSession session = startedGameSession(1L);
        when(matchGameService.isAcceptableTime(eq(10L), anyLong())).thenReturn(false);

        mockMvc.perform(post("/decks/1/match/result").session(session).param("timeMillis", "10"))
                .andExpect(status().isBadRequest());

        verify(studySessionService, never()).recordMatch(anyLong(), anyInt(), anyLong());
    }

    private void givenPlayableGame() {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(matchGameService.newGame(1L)).thenReturn(playableGame());
    }

    private MatchGame playableGame() {
        return new MatchGame(
                List.of(new MatchItem(1, "JVM"), new MatchItem(2, "JDK")),
                List.of(new MatchItem(2, "набор"), new MatchItem(1, "машина")));
    }

    private MockHttpSession startedGameSession(Long deckId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(MatchGameController.startedAtKey(deckId), Instant.now().minusSeconds(60));
        session.setAttribute(MatchGameController.pairsKey(deckId), 6);
        return session;
    }

    @Test
    void matchWithPostIsNotAllowed() throws Exception {
        mockMvc.perform(post("/decks/1/match"))
                .andExpect(status().isMethodNotAllowed());
    }
}
