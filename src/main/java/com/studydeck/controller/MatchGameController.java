package com.studydeck.controller;

import com.studydeck.dto.MatchGame;
import com.studydeck.service.DeckService;
import com.studydeck.service.MatchGameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MatchGameController {

    private final DeckService deckService;
    private final MatchGameService matchGameService;

    public MatchGameController(DeckService deckService, MatchGameService matchGameService) {
        this.deckService = deckService;
        this.matchGameService = matchGameService;
    }

    @GetMapping("/decks/{deckId}/match")
    public String match(@PathVariable Long deckId, Model model) {
        model.addAttribute("deck", deckService.getById(deckId));
        model.addAttribute("deckId", deckId);
        model.addAttribute("game", matchGameService.newGame(deckId));
        model.addAttribute("minPairs", MatchGame.MIN_PAIRS);
        return "match";
    }
}
