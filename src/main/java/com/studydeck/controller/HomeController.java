package com.studydeck.controller;

import com.studydeck.service.DeckService;
import com.studydeck.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;

@Controller
public class HomeController {

    private final DeckService deckService;
    private final ReviewService reviewService;

    public HomeController(DeckService deckService, ReviewService reviewService) {
        this.deckService = deckService;
        this.reviewService = reviewService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("decks", deckService.findAllSummaries());
        model.addAttribute("reviewCount", reviewService.countForToday(LocalDateTime.now()));
        return "index";
    }
}
