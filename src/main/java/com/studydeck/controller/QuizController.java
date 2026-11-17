package com.studydeck.controller;

import com.studydeck.dto.QuizQuestion;
import com.studydeck.service.DeckService;
import com.studydeck.service.QuizService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class QuizController {

    static final String QUIZ_PREFIX = "quiz-";

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

    static String quizKey(Long deckId) {
        return QUIZ_PREFIX + deckId;
    }
}
