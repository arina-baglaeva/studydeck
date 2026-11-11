package com.studydeck.controller;

import com.studydeck.service.CardService;
import com.studydeck.service.DeckService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class StudyController {

    private final DeckService deckService;
    private final CardService cardService;

    public StudyController(DeckService deckService, CardService cardService) {
        this.deckService = deckService;
        this.cardService = cardService;
    }

    @GetMapping("/decks/{deckId}/study")
    public String study(@PathVariable Long deckId, Model model) {
        model.addAttribute("deck", deckService.getById(deckId));
        model.addAttribute("deckId", deckId);
        model.addAttribute("cards", cardService.findByDeck(deckId));
        return "study";
    }
}
