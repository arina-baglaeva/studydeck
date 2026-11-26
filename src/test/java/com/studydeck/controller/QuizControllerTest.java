package com.studydeck.controller;

import com.studydeck.dto.QuizAnswer;
import com.studydeck.dto.QuizQuestion;
import com.studydeck.dto.QuizResult;
import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Deck;
import com.studydeck.model.StudyMode;
import com.studydeck.service.DeckService;
import com.studydeck.service.QuizService;
import com.studydeck.service.StudySessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
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

@WebMvcTest(QuizController.class)
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudySessionService studySessionService;

    @MockBean
    private DeckService deckService;

    @MockBean
    private QuizService quizService;

    @Test
    void quizPageShowsQuestionsAndOptions() throws Exception {
        givenQuiz(List.of(question("JVM", "машина", 2)));

        mockMvc.perform(get("/decks/1/quiz"))
                .andExpect(status().isOk())
                .andExpect(view().name("quiz"))
                .andExpect(content().string(containsString("JVM")))
                .andExpect(content().string(containsString("машина")))
                .andExpect(content().string(containsString("name=\"answer0\"")))
                .andExpect(content().string(containsString("value=\"3\"")))
                .andExpect(content().string(containsString("action=\"/decks/1/quiz\"")))
                .andExpect(content().string(containsString("method=\"post\"")));
    }

    @Test
    void quizPageDoesNotRevealCorrectAnswer() throws Exception {
        givenQuiz(List.of(question("JVM", "машина", 2)));

        mockMvc.perform(get("/decks/1/quiz"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("correct"))));
    }

    @Test
    void quizIsRememberedInSession() throws Exception {
        List<QuizQuestion> questions = List.of(question("JVM", "машина", 0));
        givenQuiz(questions);
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/decks/1/quiz").session(session))
                .andExpect(status().isOk());

        assertThat(session.getAttribute(QuizController.quizKey(1L))).isEqualTo(questions);
    }

    @Test
    void emptyQuizShowsMessage() throws Exception {
        givenQuiz(List.of());

        mockMvc.perform(get("/decks/1/quiz"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Для теста нужно хотя бы")))
                .andExpect(content().string(not(containsString("Проверить"))));
    }

    @Test
    void cardTextIsEscaped() throws Exception {
        givenQuiz(List.of(question("<b>x</b>", "<script>alert(1)</script>", 1)));

        mockMvc.perform(get("/decks/1/quiz"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    void missingDeckReturns404() throws Exception {
        when(deckService.getById(999L)).thenThrow(new NotFoundException("Колода не найдена"));

        mockMvc.perform(get("/decks/999/quiz"))
                .andExpect(status().isNotFound());
    }

    @Test
    void nonNumericDeckIdReturns404() throws Exception {
        mockMvc.perform(get("/decks/abc/quiz"))
                .andExpect(status().isNotFound());
    }

    @Test
    void submittedAnswersAreCheckedWithQuizFromSession() throws Exception {
        List<QuizQuestion> questions = List.of(question("JVM", "машина", 2), question("JDK", "набор", 0));
        MockHttpSession session = sessionWithQuiz(1L, questions);
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(quizService.check(questions, List.of(2, 1))).thenReturn(new QuizResult(List.of(
                new QuizAnswer("JVM", "машина", "машина", true),
                new QuizAnswer("JDK", "вариант1", "набор", false))));

        mockMvc.perform(post("/decks/1/quiz").session(session).param("answer0", "2").param("answer1", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("quiz-result"))
                .andExpect(content().string(containsString("Правильно: <span>1</span> из <span>2</span>")))
                .andExpect(content().string(containsString("50%")))
                .andExpect(content().string(containsString("набор")))
                .andExpect(content().string(containsString("— ошибка")));
    }

    @Test
    void quizCanBeCheckedOnlyOnce() throws Exception {
        List<QuizQuestion> questions = List.of(question("JVM", "машина", 2));
        MockHttpSession session = sessionWithQuiz(1L, questions);
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(quizService.check(eq(questions), anyList())).thenReturn(new QuizResult(List.of(
                new QuizAnswer("JVM", "машина", "машина", true))));

        mockMvc.perform(post("/decks/1/quiz").session(session).param("answer0", "2"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/decks/1/quiz").session(session).param("answer0", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/1/quiz"));
    }

    @Test
    void missingAndBrokenAnswersBecomeNull() throws Exception {
        List<QuizQuestion> questions = List.of(question("JVM", "машина", 2), question("JDK", "набор", 0));
        MockHttpSession session = sessionWithQuiz(1L, questions);
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(quizService.check(eq(questions), anyList())).thenReturn(new QuizResult(List.of()));

        mockMvc.perform(post("/decks/1/quiz").session(session).param("answer0", "abc"))
                .andExpect(status().isOk());

        verify(quizService).check(questions, Arrays.asList(null, null));
    }

    @Test
    void submitWithoutQuizInSessionRedirectsToNewQuiz() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));

        mockMvc.perform(post("/decks/1/quiz").param("answer0", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/1/quiz"));

        verify(quizService, never()).check(any(), any());
    }

    @Test
    void quizFromOtherDeckIsNotUsed() throws Exception {
        MockHttpSession session = sessionWithQuiz(1L, List.of(question("JVM", "машина", 2)));
        when(deckService.getById(2L)).thenReturn(new Deck("Other", null));

        mockMvc.perform(post("/decks/2/quiz").session(session).param("answer0", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/2/quiz"));

        verify(quizService, never()).check(any(), any());
    }

    @Test
    void resultTextIsEscaped() throws Exception {
        List<QuizQuestion> questions = List.of(question("JVM", "машина", 2));
        MockHttpSession session = sessionWithQuiz(1L, questions);
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(quizService.check(eq(questions), anyList())).thenReturn(new QuizResult(List.of(
                new QuizAnswer("<b>x</b>", "<script>alert(1)</script>", "y", false))));

        mockMvc.perform(post("/decks/1/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    void submitForMissingDeckReturns404() throws Exception {
        when(deckService.getById(999L)).thenThrow(new NotFoundException("Колода не найдена"));

        mockMvc.perform(post("/decks/999/quiz"))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkedQuizIsSavedAsStudySession() throws Exception {
        List<QuizQuestion> questions = List.of(question("JVM", "машина", 2), question("JDK", "набор", 0));
        MockHttpSession session = sessionWithQuiz(1L, questions);
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(quizService.check(eq(questions), anyList())).thenReturn(new QuizResult(List.of(
                new QuizAnswer("JVM", "машина", "машина", true),
                new QuizAnswer("JDK", "вариант1", "набор", false))));

        mockMvc.perform(post("/decks/1/quiz").session(session).param("answer0", "2").param("answer1", "1"))
                .andExpect(status().isOk());

        verify(studySessionService).recordAnswers(1L, StudyMode.QUIZ, 1, 2);
    }

    @Test
    void quizWithoutSessionIsNotSaved() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));

        mockMvc.perform(post("/decks/1/quiz").param("answer0", "1"))
                .andExpect(status().is3xxRedirection());

        verify(studySessionService, never()).recordAnswers(anyLong(), any(), anyInt(), anyInt());
    }

    private MockHttpSession sessionWithQuiz(Long deckId, List<QuizQuestion> questions) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(QuizController.quizKey(deckId), questions);
        return session;
    }

    private void givenQuiz(List<QuizQuestion> questions) {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(quizService.newQuiz(1L)).thenReturn(questions);
    }

    private QuizQuestion question(String term, String answer, int correctIndex) {
        List<String> options = new ArrayList<>(List.of("вариант1", "вариант2", "вариант3"));
        options.add(correctIndex, answer);
        return new QuizQuestion(1L, term, options, correctIndex);
    }
}
