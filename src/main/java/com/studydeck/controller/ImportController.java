package com.studydeck.controller;

import com.studydeck.dto.ImportPreview;
import com.studydeck.dto.ParseResult;
import com.studydeck.dto.ParsedCard;
import com.studydeck.exception.ImportException;
import com.studydeck.service.CardImportService;
import com.studydeck.service.CardService;
import com.studydeck.service.DeckService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/decks/{deckId}/import")
public class ImportController {

    static final String PREVIEW_ATTRIBUTE = "importPreview";

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
                             HttpSession session,
                             Model model) {
        deckService.getById(deckId);
        try {
            ParseResult result = cardImportService.readFile(file);
            List<ParsedCard> newCards = cardService.withoutDuplicates(deckId, result.getCards());
            int duplicateCount = result.getCards().size() - newCards.size();
            session.setAttribute(PREVIEW_ATTRIBUTE,
                    new ImportPreview(deckId, newCards, result.getBadLines(), duplicateCount));
            return "redirect:/decks/" + deckId + "/import/preview";
        } catch (ImportException e) {
            model.addAttribute("importError", e.getMessage());
            prepareForm(model, deckId);
            return "import";
        }
    }

    @GetMapping("/preview")
    public String preview(@PathVariable Long deckId, HttpSession session, Model model,
                          RedirectAttributes redirectAttributes) {
        model.addAttribute("deck", deckService.getById(deckId));
        ImportPreview preview = findPreview(session, deckId);
        if (preview == null) {
            redirectAttributes.addFlashAttribute("importError", "Сначала загрузите файл");
            return "redirect:/decks/" + deckId + "/import";
        }
        model.addAttribute("deckId", deckId);
        model.addAttribute("preview", preview);
        return "import-preview";
    }

    @PostMapping("/confirm")
    public String confirm(@PathVariable Long deckId,
                          @RequestParam(name = "selected", required = false) List<Integer> selected,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        deckService.getById(deckId);
        ImportPreview preview = findPreview(session, deckId);
        if (preview == null) {
            redirectAttributes.addFlashAttribute("importError", "Предпросмотр устарел, загрузите файл ещё раз");
            return "redirect:/decks/" + deckId + "/import";
        }
        List<ParsedCard> cards = preview.selectCards(selected);
        if (cards.isEmpty()) {
            redirectAttributes.addFlashAttribute("previewError", "Отметьте хотя бы одну карточку");
            return "redirect:/decks/" + deckId + "/import/preview";
        }
        int added = cardService.addCards(deckId, cards);
        session.removeAttribute(PREVIEW_ATTRIBUTE);
        redirectAttributes.addFlashAttribute("addedCount", added);
        redirectAttributes.addFlashAttribute("badLines", preview.getBadLines());
        return "redirect:/decks/" + deckId + "/import";
    }

    @PostMapping("/cancel")
    public String cancel(@PathVariable Long deckId, HttpSession session) {
        session.removeAttribute(PREVIEW_ATTRIBUTE);
        return "redirect:/decks/" + deckId;
    }

    private ImportPreview findPreview(HttpSession session, Long deckId) {
        Object value = session.getAttribute(PREVIEW_ATTRIBUTE);
        if (value instanceof ImportPreview preview && preview.belongsTo(deckId)) {
            return preview;
        }
        return null;
    }

    private void prepareForm(Model model, Long deckId) {
        model.addAttribute("deck", deckService.getById(deckId));
        model.addAttribute("deckId", deckId);
        model.addAttribute("maxLines", CardImportService.MAX_LINES);
    }
}
