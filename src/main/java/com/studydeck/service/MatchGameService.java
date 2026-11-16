package com.studydeck.service;

import com.studydeck.dto.MatchGame;
import com.studydeck.dto.MatchItem;
import com.studydeck.model.Card;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
public class MatchGameService {

    public static final int MAX_PAIRS = 6;
    public static final long MIN_TIME_MILLIS = 1000;
    public static final long MAX_TIME_MILLIS = 60 * 60 * 1000;
    public static final long CLOCK_TOLERANCE_MILLIS = 2000;

    private final CardService cardService;
    private final Random random = new Random();

    public MatchGameService(CardService cardService) {
        this.cardService = cardService;
    }

    public MatchGame newGame(Long deckId) {
        List<Card> cards = new ArrayList<>(cardService.findByDeck(deckId));
        Collections.shuffle(cards, random);
        List<Card> chosen = pickUniquePairs(cards);

        List<MatchItem> terms = new ArrayList<>();
        List<MatchItem> definitions = new ArrayList<>();
        for (int i = 0; i < chosen.size(); i++) {
            Card card = chosen.get(i);
            terms.add(new MatchItem(i + 1, card.getTerm()));
            definitions.add(new MatchItem(i + 1, card.getDefinition()));
        }
        Collections.shuffle(definitions, random);
        return new MatchGame(terms, definitions);
    }

    public boolean isAcceptableTime(long timeMillis, long serverElapsedMillis) {
        return timeMillis >= MIN_TIME_MILLIS
                && timeMillis <= MAX_TIME_MILLIS
                && timeMillis <= serverElapsedMillis + CLOCK_TOLERANCE_MILLIS;
    }

    private List<Card> pickUniquePairs(List<Card> cards) {
        List<Card> chosen = new ArrayList<>();
        Set<String> usedTerms = new HashSet<>();
        Set<String> usedDefinitions = new HashSet<>();
        for (Card card : cards) {
            if (chosen.size() == MAX_PAIRS) {
                break;
            }
            String term = TextUtils.normalize(card.getTerm());
            String definition = TextUtils.normalize(card.getDefinition());
            if (!usedTerms.contains(term) && !usedDefinitions.contains(definition)) {
                usedTerms.add(term);
                usedDefinitions.add(definition);
                chosen.add(card);
            }
        }
        return chosen;
    }
}
