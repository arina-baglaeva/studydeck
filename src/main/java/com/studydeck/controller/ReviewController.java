package com.studydeck.controller;

import com.studydeck.model.Card;
import com.studydeck.model.Grade;
import com.studydeck.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/review")
    public String review(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("card", reviewService.nextCard(now).orElse(null));
        model.addAttribute("remaining", reviewService.countForToday(now));
        model.addAttribute("answerMaxLength", Card.DEFINITION_MAX_LENGTH);
        model.addAttribute("revealed", false);
        return "review";
    }

    @PostMapping("/review/{cardId}/answer")
    public String reveal(@PathVariable Long cardId,
                         @RequestParam(name = "answer", required = false) String answer,
                         Model model) {
        LocalDateTime now = LocalDateTime.now();
        Optional<Card> card = reviewService.findWaitingCard(cardId, now);
        if (card.isEmpty()) {
            return "redirect:/review";
        }
        model.addAttribute("card", card.get());
        model.addAttribute("recall", reviewService.checkRecall(card.get(), answer));
        model.addAttribute("remaining", reviewService.countForToday(now));
        model.addAttribute("grades", Grade.values());
        model.addAttribute("revealed", true);
        return "review";
    }

    @PostMapping("/review/{cardId}")
    public String grade(@PathVariable Long cardId, @RequestParam Grade grade) {
        reviewService.grade(cardId, grade, LocalDateTime.now());
        return "redirect:/review";
    }
}
