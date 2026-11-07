package com.studydeck.controller;

import com.studydeck.dto.ParseResult;
import com.studydeck.exception.ImportException;
import com.studydeck.service.CardImportService;
import com.studydeck.service.CardService;
import com.studydeck.service.DeckService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/decks/{deckId}/import")
public class ImportController {

    private final DeckService deckService;
    private final CardService cardService;
    private final CardImportService cardImportService;

    public ImportController(DeckService deckService, CardService cardService, CardImportService cardImportService) {
        this.deckService = deckService;
        this.cardService = cardService;
        this.cardImportService = cardImportService;
    }

    @GetMapping
    public String importForm(@PathVariable Long deckId, Model model) {
        prepareForm(model, deckId);
        return "import";
    }

    @PostMapping
    public String importFile(@PathVariable Long deckId,
                             @RequestParam(name = "file", required = false) MultipartFile file,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        deckService.getById(deckId);
        try {
            ParseResult result = cardImportService.readFile(file);
            int added = cardService.addCards(deckId, result.getCards());
            redirectAttributes.addFlashAttribute("addedCount", added);
            redirectAttributes.addFlashAttribute("badLines", result.getBadLines());
            return "redirect:/decks/" + deckId + "/import";
        } catch (ImportException e) {
            model.addAttribute("importError", e.getMessage());
            prepareForm(model, deckId);
            return "import";
        }
    }

    private void prepareForm(Model model, Long deckId) {
        model.addAttribute("deck", deckService.getById(deckId));
        model.addAttribute("deckId", deckId);
        model.addAttribute("maxLines", CardImportService.MAX_LINES);
    }
}
