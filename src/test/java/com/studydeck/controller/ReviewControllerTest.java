package com.studydeck.controller;

import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Card;
import com.studydeck.model.Deck;
import com.studydeck.model.Grade;
import com.studydeck.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Test
    void reviewPageShowsNextCardAndGradeButtons() throws Exception {
        givenNextCard("JVM", "виртуальная машина");
        when(reviewService.countForToday(any())).thenReturn(4L);

        mockMvc.perform(get("/review"))
                .andExpect(status().isOk())
                .andExpect(view().name("review"))
                .andExpect(content().string(containsString("JVM")))
                .andExpect(content().string(containsString("виртуальная машина")))
                .andExpect(content().string(containsString("Осталось карточек: <span class=\"review-remaining\">4</span>")))
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("value=\"AGAIN\"")))
                .andExpect(content().string(containsString("Снова")))
                .andExpect(content().string(containsString("Трудно")))
                .andExpect(content().string(containsString("Хорошо")))
                .andExpect(content().string(containsString("Легко")));
    }

    @Test
    void reviewPageSaysAllDoneWhenNoCards() throws Exception {
        when(reviewService.nextCard(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/review"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("На сегодня всё повторено")))
                .andExpect(content().string(not(containsString("Показать ответ"))));
    }

    @Test
    void cardTextIsEscaped() throws Exception {
        givenNextCard("<b>x</b>", "<script>alert(1)</script>");

        mockMvc.perform(get("/review"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    void gradeIsSavedAndNextCardIsShown() throws Exception {
        mockMvc.perform(post("/review/5").param("grade", "GOOD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/review"));

        verify(reviewService).grade(eq(5L), eq(Grade.GOOD), any());
    }

    @Test
    void unknownGradeIsRejected() throws Exception {
        mockMvc.perform(post("/review/5").param("grade", "PERFECT"))
                .andExpect(status().is4xxClientError());

        verify(reviewService, never()).grade(any(), any(), any());
    }

    @Test
    void missingGradeIsRejected() throws Exception {
        mockMvc.perform(post("/review/5"))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).grade(any(), any(), any());
    }

    @Test
    void missingCardReturns404() throws Exception {
        when(reviewService.grade(eq(999L), any(), any())).thenThrow(new NotFoundException("Карточка не найдена"));

        mockMvc.perform(post("/review/999").param("grade", "GOOD"))
                .andExpect(status().isNotFound());
    }

    @Test
    void nonNumericCardIdReturns404() throws Exception {
        mockMvc.perform(post("/review/abc").param("grade", "GOOD"))
                .andExpect(status().isNotFound());
    }

    private void givenNextCard(String term, String definition) {
        Deck deck = new Deck("Java", null);
        Card card = new Card(term, definition);
        deck.addCard(card);
        when(reviewService.nextCard(any())).thenReturn(Optional.of(card));
    }
}
