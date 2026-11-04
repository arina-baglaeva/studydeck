package com.studydeck.controller;

import com.studydeck.dto.CardForm;
import com.studydeck.model.Card;
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
@RequestMapping("/decks/{deckId}/cards")
public class CardController {

    private final DeckService deckService;
    private final CardService cardService;

    public CardController(DeckService deckService, CardService cardService) {
        this.deckService = deckService;
        this.cardService = cardService;
    }

    @GetMapping("/new")
    public String newCardForm(@PathVariable Long deckId, Model model) {
        model.addAttribute("cardForm", new CardForm());
        prepareForm(model, deckId, null);
        return "card-form";
    }

    @PostMapping
    public String addCard(@PathVariable Long deckId,
                          @Valid @ModelAttribute("cardForm") CardForm cardForm,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, deckId, null);
            return "card-form";
        }
        cardService.addCard(deckId, cardForm.getTerm(), cardForm.getDefinition());
        return "redirect:/decks/" + deckId;
    }

    @GetMapping("/{cardId}/edit")
    public String editCardForm(@PathVariable Long deckId, @PathVariable Long cardId, Model model) {
        Card card = cardService.getCard(deckId, cardId);
        model.addAttribute("cardForm", CardForm.fromCard(card));
        prepareForm(model, deckId, cardId);
        return "card-form";
    }

    @PostMapping("/{cardId}")
    public String updateCard(@PathVariable Long deckId,
                             @PathVariable Long cardId,
                             @Valid @ModelAttribute("cardForm") CardForm cardForm,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            cardService.getCard(deckId, cardId);
            prepareForm(model, deckId, cardId);
            return "card-form";
        }
        cardService.updateCard(deckId, cardId, cardForm.getTerm(), cardForm.getDefinition());
        return "redirect:/decks/" + deckId;
    }

    private void prepareForm(Model model, Long deckId, Long cardId) {
        model.addAttribute("deck", deckService.getById(deckId));
        model.addAttribute("deckId", deckId);
        model.addAttribute("cardId", cardId);
        model.addAttribute("termMaxLength", Card.TERM_MAX_LENGTH);
        model.addAttribute("definitionMaxLength", Card.DEFINITION_MAX_LENGTH);
    }
}
