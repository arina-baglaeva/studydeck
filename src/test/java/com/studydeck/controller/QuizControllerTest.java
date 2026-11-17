package com.studydeck.controller;

import com.studydeck.dto.QuizQuestion;
import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Deck;
import com.studydeck.service.DeckService;
import com.studydeck.service.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(QuizController.class)
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
