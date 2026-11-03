package com.studydeck.controller;

import com.studydeck.dto.DeckForm;
import com.studydeck.model.Deck;
import com.studydeck.service.DeckService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping("/new")
    public String newDeckForm(Model model) {
        model.addAttribute("deckForm", new DeckForm());
        addLengthLimits(model);
        return "deck-form";
    }

    @PostMapping
    public String createDeck(@Valid @ModelAttribute("deckForm") DeckForm deckForm,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            addLengthLimits(model);
            return "deck-form";
        }
        deckService.create(deckForm.getTitle(), deckForm.getDescription());
        return "redirect:/";
    }

    private void addLengthLimits(Model model) {
        model.addAttribute("titleMaxLength", Deck.TITLE_MAX_LENGTH);
        model.addAttribute("descriptionMaxLength", Deck.DESCRIPTION_MAX_LENGTH);
    }
}
