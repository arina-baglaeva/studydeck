package com.studydeck.controller;

import com.studydeck.service.DeckService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final DeckService deckService;

    public HomeController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("decks", deckService.findAllSummaries());
        return "index";
    }
}
