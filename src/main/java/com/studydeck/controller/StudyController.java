package com.studydeck.controller;

import com.studydeck.service.DeckService;
import com.studydeck.service.StudyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StudyController {

    private final DeckService deckService;
    private final StudyService studyService;

    public StudyController(DeckService deckService, StudyService studyService) {
        this.deckService = deckService;
        this.studyService = studyService;
    }

    @GetMapping("/decks/{deckId}/study")
    public String study(@PathVariable Long deckId,
                        @RequestParam(defaultValue = "false") boolean shuffle,
                        Model model) {
        model.addAttribute("deck", deckService.getById(deckId));
        model.addAttribute("deckId", deckId);
        model.addAttribute("shuffle", shuffle);
        model.addAttribute("cards", studyService.cardsForStudy(deckId, shuffle));
        return "study";
    }
}
