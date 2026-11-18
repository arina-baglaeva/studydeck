package com.studydeck.controller;

import com.studydeck.dto.QuizAnswer;
import com.studydeck.dto.QuizResult;
import com.studydeck.dto.WriteQuestion;
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

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

@WebMvcTest(WriteController.class)
class WriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeckService deckService;

    @MockBean
    private QuizService quizService;

    @Test
    void writePageShowsPromptsAndInputs() throws Exception {
        givenQuestions(List.of(new WriteQuestion(1L, "виртуальная машина", "JVM")));

        mockMvc.perform(get("/decks/1/write"))
                .andExpect(status().isOk())
                .andExpect(view().name("write"))
                .andExpect(content().string(containsString("виртуальная машина")))
                .andExpect(content().string(containsString("name=\"answer0\"")))
                .andExpect(content().string(containsString("maxlength=\"200\"")))
                .andExpect(content().string(containsString("action=\"/decks/1/write\"")))
                .andExpect(content().string(containsString("method=\"post\"")));
    }

    @Test
    void writePageDoesNotRevealAnswer() throws Exception {
        givenQuestions(List.of(new WriteQuestion(1L, "виртуальная машина", "JVM")));

        mockMvc.perform(get("/decks/1/write"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("JVM"))));
    }

    @Test
    void questionsAreRememberedInSession() throws Exception {
        List<WriteQuestion> questions = List.of(new WriteQuestion(1L, "машина", "JVM"));
        givenQuestions(questions);
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/decks/1/write").session(session))
                .andExpect(status().isOk());

        assertThat(session.getAttribute(WriteController.writeKey(1L))).isEqualTo(questions);
    }

    @Test
    void emptyDeckShowsMessage() throws Exception {
        givenQuestions(List.of());

        mockMvc.perform(get("/decks/1/write"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("В колоде пока нет карточек")))
                .andExpect(content().string(not(containsString("Проверить"))));
    }

    @Test
    void promptIsEscaped() throws Exception {
        givenQuestions(List.of(new WriteQuestion(1L, "<script>alert(1)</script>", "x")));

        mockMvc.perform(get("/decks/1/write"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    void missingDeckReturns404() throws Exception {
        when(deckService.getById(999L)).thenThrow(new NotFoundException("Колода не найдена"));

        mockMvc.perform(get("/decks/999/write"))
                .andExpect(status().isNotFound());
    }

    @Test
    void typedAnswersAreCheckedWithQuestionsFromSession() throws Exception {
        List<WriteQuestion> questions = List.of(new WriteQuestion(1L, "машина", "JVM"),
                new WriteQuestion(2L, "набор", "JDK"));
        MockHttpSession session = sessionWithQuestions(1L, questions);
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(quizService.checkWritten(questions, List.of(" jvm ", "JRE"))).thenReturn(new QuizResult(List.of(
                new QuizAnswer("машина", "jvm", "JVM", true),
                new QuizAnswer("набор", "JRE", "JDK", false))));

        mockMvc.perform(post("/decks/1/write").session(session).param("answer0", " jvm ").param("answer1", "JRE"))
                .andExpect(status().isOk())
                .andExpect(view().name("quiz-result"))
                .andExpect(content().string(containsString("Результат: напиши ответ")))
                .andExpect(content().string(containsString("50%")))
                .andExpect(content().string(containsString("href=\"/decks/1/write\"")))
                .andExpect(content().string(containsString("— ошибка")));
    }

    @Test
    void missingAnswersBecomeNull() throws Exception {
        List<WriteQuestion> questions = List.of(new WriteQuestion(1L, "машина", "JVM"),
                new WriteQuestion(2L, "набор", "JDK"));
        MockHttpSession session = sessionWithQuestions(1L, questions);
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(quizService.checkWritten(eq(questions), anyList())).thenReturn(new QuizResult(List.of()));

        mockMvc.perform(post("/decks/1/write").session(session).param("answer5", "JVM"))
                .andExpect(status().isOk());

        verify(quizService).checkWritten(questions, Arrays.asList(null, null));
    }

    @Test
    void questionsCanBeCheckedOnlyOnce() throws Exception {
        List<WriteQuestion> questions = List.of(new WriteQuestion(1L, "машина", "JVM"));
        MockHttpSession session = sessionWithQuestions(1L, questions);
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(quizService.checkWritten(eq(questions), anyList())).thenReturn(new QuizResult(List.of(
                new QuizAnswer("машина", "JVM", "JVM", true))));

        mockMvc.perform(post("/decks/1/write").session(session).param("answer0", "JVM"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/decks/1/write").session(session).param("answer0", "JVM"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/1/write"));
    }

    @Test
    void questionsFromOtherDeckAreNotUsed() throws Exception {
        MockHttpSession session = sessionWithQuestions(1L, List.of(new WriteQuestion(1L, "машина", "JVM")));
        when(deckService.getById(2L)).thenReturn(new Deck("Other", null));

        mockMvc.perform(post("/decks/2/write").session(session).param("answer0", "JVM"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/2/write"));

        verify(quizService, never()).checkWritten(any(), any());
    }

    @Test
    void typedAnswerIsEscapedOnResultPage() throws Exception {
        List<WriteQuestion> questions = List.of(new WriteQuestion(1L, "машина", "JVM"));
        MockHttpSession session = sessionWithQuestions(1L, questions);
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(quizService.checkWritten(eq(questions), anyList())).thenReturn(new QuizResult(List.of(
                new QuizAnswer("машина", "<script>alert(1)</script>", "JVM", false))));

        mockMvc.perform(post("/decks/1/write").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    private MockHttpSession sessionWithQuestions(Long deckId, List<WriteQuestion> questions) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(WriteController.writeKey(deckId), questions);
        return session;
    }

    private void givenQuestions(List<WriteQuestion> questions) {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(quizService.newWriteQuiz(1L)).thenReturn(questions);
    }
}
