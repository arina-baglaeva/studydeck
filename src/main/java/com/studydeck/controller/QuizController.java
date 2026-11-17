package com.studydeck.controller;

import com.studydeck.dto.QuizQuestion;
import com.studydeck.dto.QuizResult;
import com.studydeck.service.DeckService;
import com.studydeck.service.QuizService;
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
public class QuizController {

    static final String QUIZ_PREFIX = "quiz-";
    static final String ANSWER_PREFIX = "answer";

    private final DeckService deckService;
    private final QuizService quizService;

    public QuizController(DeckService deckService, QuizService quizService) {
        this.deckService = deckService;
        this.quizService = quizService;
    }

    @GetMapping("/decks/{deckId}/quiz")
    public String quiz(@PathVariable Long deckId, Model model, HttpSession session) {
        model.addAttribute("deck", deckService.getById(deckId));
        model.addAttribute("deckId", deckId);
        List<QuizQuestion> questions = quizService.newQuiz(deckId);
        model.addAttribute("questions", questions);
        model.addAttribute("minCards", QuizService.OPTION_COUNT);
        session.setAttribute(quizKey(deckId), questions);
        return "quiz";
    }

    @PostMapping("/decks/{deckId}/quiz")
    public String check(@PathVariable Long deckId,
                        @RequestParam Map<String, String> params,
                        Model model,
                        HttpSession session) {
        model.addAttribute("deck", deckService.getById(deckId));
        model.addAttribute("deckId", deckId);
        List<QuizQuestion> questions = takeQuizFromSession(deckId, session);
        if (questions.isEmpty()) {
            return "redirect:/decks/" + deckId + "/quiz";
        }
        QuizResult result = quizService.check(questions, chosenIndexes(params, questions.size()));
        model.addAttribute("result", result);
        return "quiz-result";
    }

    static String quizKey(Long deckId) {
        return QUIZ_PREFIX + deckId;
    }

    private List<QuizQuestion> takeQuizFromSession(Long deckId, HttpSession session) {
        Object saved = session.getAttribute(quizKey(deckId));
        session.removeAttribute(quizKey(deckId));
        List<QuizQuestion> questions = new ArrayList<>();
        if (saved instanceof List<?> savedList) {
            for (Object item : savedList) {
                if (item instanceof QuizQuestion question) {
                    questions.add(question);
                }
            }
        }
        return questions;
    }

    private List<Integer> chosenIndexes(Map<String, String> params, int questionCount) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < questionCount; i++) {
            indexes.add(parseIndex(params.get(ANSWER_PREFIX + i)));
        }
        return indexes;
    }

    private Integer parseIndex(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
