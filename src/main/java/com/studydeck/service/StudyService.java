package com.studydeck.service;

import com.studydeck.model.Card;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class StudyService {

    private final CardService cardService;
    private final Random random = new Random();

    public StudyService(CardService cardService) {
        this.cardService = cardService;
    }

    public List<Card> cardsForStudy(Long deckId, boolean shuffle) {
        List<Card> cards = new ArrayList<>(cardService.findByDeck(deckId));
        if (shuffle) {
            Collections.shuffle(cards, random);
        }
        return cards;
    }
}
