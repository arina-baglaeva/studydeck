package com.studydeck.controller;

import com.studydeck.model.Grade;
import com.studydeck.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

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
        model.addAttribute("grades", Grade.values());
        return "review";
    }

    @PostMapping("/review/{cardId}")
    public String grade(@PathVariable Long cardId, @RequestParam Grade grade) {
        reviewService.grade(cardId, grade, LocalDateTime.now());
        return "redirect:/review";
    }
}
