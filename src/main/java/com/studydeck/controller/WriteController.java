package com.studydeck.controller;

import com.studydeck.dto.QuizResult;
import com.studydeck.dto.WriteQuestion;
import com.studydeck.model.Card;
import com.studydeck.service.DeckService;
import com.studydeck.model.StudyMode;
import com.studydeck.service.QuizService;
import com.studydeck.service.StudySessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class WriteController {

    static final String WRITE_PREFIX = "write-";
    static final String ANSWER_PREFIX = "answer";

    private final DeckService deckService;
    private final QuizService quizService;
    private final StudySessionService studySessionService;

    public WriteController(DeckService deckService, QuizService quizService,
                          StudySessionService studySessionService) {
        this.deckService = deckService;
        this.quizService = quizService;
        this.studySessionService = studySessionService;
    }

    @GetMapping("/decks/{deckId}/write")
    public String write(@PathVariable Long deckId, Model model, HttpSession session) {
        model.addAttribute("deck", deckService.getById(deckId));
        model.addAttribute("deckId", deckId);
        List<WriteQuestion> questions = quizService.newWriteQuiz(deckId);
        model.addAttribute("questions", questions);
        model.addAttribute("answerMaxLength", Card.TERM_MAX_LENGTH);
        session.setAttribute(writeKey(deckId), questions);
        return "write";
    }

    @PostMapping("/decks/{deckId}/write")
    public String check(@PathVariable Long deckId,
                        @RequestParam Map<String, String> params,
                        Model model,
                        HttpSession session) {
        model.addAttribute("deck", deckService.getById(deckId));
        model.addAttribute("deckId", deckId);
        List<WriteQuestion> questions = takeQuestionsFromSession(deckId, session);
        if (questions.isEmpty()) {
            return "redirect:/decks/" + deckId + "/write";
        }
        QuizResult result = quizService.checkWritten(questions, typedAnswers(params, questions.size()));
        studySessionService.recordAnswers(deckId, StudyMode.WRITE, result.getCorrectCount(), result.getTotal());
        model.addAttribute("result", result);
        model.addAttribute("mode", "write");
        return "quiz-result";
    }

    static String writeKey(Long deckId) {
        return WRITE_PREFIX + deckId;
    }

    private List<WriteQuestion> takeQuestionsFromSession(Long deckId, HttpSession session) {
        Object saved = session.getAttribute(writeKey(deckId));
        session.removeAttribute(writeKey(deckId));
        List<WriteQuestion> questions = new ArrayList<>();
        if (saved instanceof List<?> savedList) {
            for (Object item : savedList) {
                if (item instanceof WriteQuestion question) {
                    questions.add(question);
                }
            }
        }
        return questions;
    }

    private List<String> typedAnswers(Map<String, String> params, int questionCount) {
        List<String> answers = new ArrayList<>();
        for (int i = 0; i < questionCount; i++) {
            answers.add(params.get(ANSWER_PREFIX + i));
        }
        return answers;
    }
}
