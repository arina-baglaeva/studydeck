package com.studydeck.controller;

import com.studydeck.dto.MatchGame;
import com.studydeck.dto.MatchResult;
import com.studydeck.model.Deck;
import com.studydeck.service.DeckService;
import com.studydeck.service.MatchGameService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Duration;
import java.time.Instant;

@Controller
public class MatchGameController {

    static final String STARTED_AT_PREFIX = "matchStartedAt-";

    private final DeckService deckService;
    private final MatchGameService matchGameService;

    public MatchGameController(DeckService deckService, MatchGameService matchGameService) {
        this.deckService = deckService;
        this.matchGameService = matchGameService;
    }

    @GetMapping("/decks/{deckId}/match")
    public String match(@PathVariable Long deckId, Model model, HttpSession session) {
        Deck deck = deckService.getById(deckId);
        model.addAttribute("deck", deck);
        model.addAttribute("deckId", deckId);
        model.addAttribute("game", matchGameService.newGame(deckId));
        model.addAttribute("minPairs", MatchGame.MIN_PAIRS);
        model.addAttribute("bestTime", formatBestTime(deck.getBestMatchTimeMillis()));
        session.setAttribute(startedAtKey(deckId), Instant.now());
        return "match";
    }

    @PostMapping("/decks/{deckId}/match/result")
    @ResponseBody
    public ResponseEntity<MatchResult> saveResult(@PathVariable Long deckId,
                                                  @RequestParam long timeMillis,
                                                  HttpSession session) {
        Object startedAt = session.getAttribute(startedAtKey(deckId));
        session.removeAttribute(startedAtKey(deckId));
        if (!(startedAt instanceof Instant start)) {
            return ResponseEntity.badRequest().build();
        }
        long serverElapsed = Duration.between(start, Instant.now()).toMillis();
        if (!matchGameService.isAcceptableTime(timeMillis, serverElapsed)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(deckService.recordMatchTime(deckId, timeMillis));
    }

    static String startedAtKey(Long deckId) {
        return STARTED_AT_PREFIX + deckId;
    }

    private String formatBestTime(Long bestTimeMillis) {
        if (bestTimeMillis == null) {
            return null;
        }
        return MatchResult.formatTime(bestTimeMillis);
    }
}
