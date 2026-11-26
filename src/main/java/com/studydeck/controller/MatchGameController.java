package com.studydeck.controller;

import com.studydeck.dto.MatchGame;
import com.studydeck.dto.MatchResult;
import com.studydeck.model.Deck;
import com.studydeck.service.DeckService;
import com.studydeck.service.MatchGameService;
import com.studydeck.service.StudySessionService;
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
    static final String PAIRS_PREFIX = "matchPairs-";

    private final DeckService deckService;
    private final MatchGameService matchGameService;
    private final StudySessionService studySessionService;

    public MatchGameController(DeckService deckService, MatchGameService matchGameService,
                               StudySessionService studySessionService) {
        this.deckService = deckService;
        this.matchGameService = matchGameService;
        this.studySessionService = studySessionService;
    }

    @GetMapping("/decks/{deckId}/match")
    public String match(@PathVariable Long deckId, Model model, HttpSession session) {
        Deck deck = deckService.getById(deckId);
        model.addAttribute("deck", deck);
        model.addAttribute("deckId", deckId);
        MatchGame game = matchGameService.newGame(deckId);
        model.addAttribute("game", game);
        model.addAttribute("minPairs", MatchGame.MIN_PAIRS);
        model.addAttribute("bestTime", formatBestTime(deck.getBestMatchTimeMillis()));
        session.setAttribute(startedAtKey(deckId), Instant.now());
        session.setAttribute(pairsKey(deckId), game.getPairCount());
        return "match";
    }

    @PostMapping("/decks/{deckId}/match/result")
    @ResponseBody
    public ResponseEntity<MatchResult> saveResult(@PathVariable Long deckId,
                                                  @RequestParam long timeMillis,
                                                  HttpSession session) {
        Object startedAt = session.getAttribute(startedAtKey(deckId));
        Object pairs = session.getAttribute(pairsKey(deckId));
        session.removeAttribute(startedAtKey(deckId));
        session.removeAttribute(pairsKey(deckId));
        if (!(startedAt instanceof Instant start) || !(pairs instanceof Integer pairCount)) {
            return ResponseEntity.badRequest().build();
        }
        if (pairCount < MatchGame.MIN_PAIRS) {
            return ResponseEntity.badRequest().build();
        }
        long serverElapsed = Duration.between(start, Instant.now()).toMillis();
        if (!matchGameService.isAcceptableTime(timeMillis, serverElapsed)) {
            return ResponseEntity.badRequest().build();
        }
        MatchResult result = deckService.recordMatchTime(deckId, timeMillis);
        studySessionService.recordMatch(deckId, pairCount, timeMillis);
        return ResponseEntity.ok(result);
    }

    static String startedAtKey(Long deckId) {
        return STARTED_AT_PREFIX + deckId;
    }

    static String pairsKey(Long deckId) {
        return PAIRS_PREFIX + deckId;
    }

    private String formatBestTime(Long bestTimeMillis) {
        if (bestTimeMillis == null) {
            return null;
        }
        return MatchResult.formatTime(bestTimeMillis);
    }
}
