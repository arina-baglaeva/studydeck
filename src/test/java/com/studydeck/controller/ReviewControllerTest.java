package com.studydeck.controller;

import com.studydeck.dto.RecallResult;
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
    void reviewPageAsksToRecallBeforeShowingAnswer() throws Exception {
        givenNextCard("JVM", "виртуальная машина");
        when(reviewService.countForToday(any())).thenReturn(4L);

        mockMvc.perform(get("/review"))
                .andExpect(status().isOk())
                .andExpect(view().name("review"))
                .andExpect(content().string(containsString("JVM")))
                .andExpect(content().string(containsString("Осталось карточек: <span class=\"review-remaining\">4</span>")))
                .andExpect(content().string(containsString("Сначала вспомните определение")))
                .andExpect(content().string(containsString("maxlength=\"1000\"")))
                .andExpect(content().string(containsString("Показать ответ")))
                .andExpect(content().string(not(containsString("виртуальная машина"))))
                .andExpect(content().string(not(containsString("value=\"AGAIN\""))));
    }

    @Test
    void answerPageShowsDefinitionAndGradeButtons() throws Exception {
        Card card = givenWaitingCard("JVM", "виртуальная машина");
        when(reviewService.checkRecall(card, "машина")).thenReturn(new RecallResult("машина", false));

        mockMvc.perform(post("/review/5/answer").param("answer", "машина"))
                .andExpect(status().isOk())
                .andExpect(view().name("review"))
                .andExpect(content().string(containsString("виртуальная машина")))
                .andExpect(content().string(containsString("Ваш ответ")))
                .andExpect(content().string(containsString("не совпал дословно")))
                .andExpect(content().string(containsString("value=\"AGAIN\"")))
                .andExpect(content().string(containsString("Снова")))
                .andExpect(content().string(containsString("Трудно")))
                .andExpect(content().string(containsString("Хорошо")))
                .andExpect(content().string(containsString("Легко")))
                .andExpect(content().string(not(containsString("Сначала вспомните определение"))));
    }

    @Test
    void matchingAnswerIsPraised() throws Exception {
        Card card = givenWaitingCard("JVM", "машина");
        when(reviewService.checkRecall(card, "Машина")).thenReturn(new RecallResult("Машина", true));

        mockMvc.perform(post("/review/5/answer").param("answer", "Машина"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Совпадает с ответом")))
                .andExpect(content().string(not(containsString("Ваш ответ"))));
    }

    @Test
    void answerCanBeRecalledWithoutTyping() throws Exception {
        Card card = givenWaitingCard("JVM", "машина");
        when(reviewService.checkRecall(card, null)).thenReturn(RecallResult.notTyped());

        mockMvc.perform(post("/review/5/answer"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"GOOD\"")))
                .andExpect(content().string(not(containsString("Ваш ответ"))))
                .andExpect(content().string(not(containsString("Совпадает с ответом"))));
    }

    @Test
    void typedAnswerIsEscaped() throws Exception {
        Card card = givenWaitingCard("JVM", "машина");
        String typed = "<script>alert(1)</script>";
        when(reviewService.checkRecall(card, typed)).thenReturn(new RecallResult(typed, false));

        mockMvc.perform(post("/review/5/answer").param("answer", typed))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(typed))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    void answerForCardThatIsNotDueRedirectsBack() throws Exception {
        when(reviewService.findWaitingCard(eq(5L), any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/review/5/answer").param("answer", "машина"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/review"));
    }

    @Test
    void answerForMissingCardReturns404() throws Exception {
        when(reviewService.findWaitingCard(eq(999L), any())).thenThrow(new NotFoundException("Карточка не найдена"));

        mockMvc.perform(post("/review/999/answer"))
                .andExpect(status().isNotFound());
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
    void reviewPageExplainsDailyNewCardLimit() throws Exception {
        when(reviewService.nextCard(any())).thenReturn(Optional.empty());
        when(reviewService.hasNewCardsWaitingForTomorrow(any())).thenReturn(true);
        when(reviewService.getNewCardsPerDay()).thenReturn(20);

        mockMvc.perform(get("/review"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Новые карточки ждут завтра")))
                .andExpect(content().string(containsString("<span class=\"review-new-limit\">20</span>")));
    }

    @Test
    void limitMessageIsHiddenWhenNoNewCardsWait() throws Exception {
        when(reviewService.nextCard(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/review"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Новые карточки ждут завтра"))));
    }

    @Test
    void cardTextIsEscaped() throws Exception {
        givenNextCard("<script>alert(1)</script>", "<b>x</b>");

        mockMvc.perform(get("/review"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    void definitionIsEscapedOnAnswerPage() throws Exception {
        Card card = givenWaitingCard("JVM", "<script>alert(1)</script>");
        when(reviewService.checkRecall(card, null)).thenReturn(RecallResult.notTyped());

        mockMvc.perform(post("/review/5/answer"))
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

    private Card givenWaitingCard(String term, String definition) {
        Deck deck = new Deck("Java", null);
        Card card = new Card(term, definition);
        deck.addCard(card);
        when(reviewService.findWaitingCard(eq(5L), any())).thenReturn(Optional.of(card));
        return card;
    }

    private void givenNextCard(String term, String definition) {
        Deck deck = new Deck("Java", null);
        Card card = new Card(term, definition);
        deck.addCard(card);
        when(reviewService.nextCard(any())).thenReturn(Optional.of(card));
    }
}
