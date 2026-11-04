package com.studydeck.controller;

import com.studydeck.dto.DeckForm;
import com.studydeck.model.Deck;
import com.studydeck.service.CardService;
import com.studydeck.service.DeckService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/decks")
public class DeckController {

    private final DeckService deckService;
    private final CardService cardService;

    public DeckController(DeckService deckService, CardService cardService) {
        this.deckService = deckService;
        this.cardService = cardService;
    }

    @GetMapping("/{id}")
    public String showDeck(@PathVariable Long id, Model model) {
        model.addAttribute("deck", deckService.getById(id));
        model.addAttribute("deckId", id);
        model.addAttribute("cards", cardService.findByDeck(id));
        return "deck";
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
        Deck deck = deckService.create(deckForm.getTitle(), deckForm.getDescription());
        return "redirect:/decks/" + deck.getId();
    }

    private void addLengthLimits(Model model) {
        model.addAttribute("titleMaxLength", Deck.TITLE_MAX_LENGTH);
        model.addAttribute("descriptionMaxLength", Deck.DESCRIPTION_MAX_LENGTH);
    }
}
