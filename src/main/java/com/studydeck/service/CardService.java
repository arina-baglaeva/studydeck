package com.studydeck.service;

import com.studydeck.dto.ParsedCard;
import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Card;
import com.studydeck.model.Deck;
import com.studydeck.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class CardService {

    public static final int SEARCH_MAX_LENGTH = 100;

    private final CardRepository cardRepository;
    private final DeckService deckService;

    public CardService(CardRepository cardRepository, DeckService deckService) {
        this.cardRepository = cardRepository;
        this.deckService = deckService;
    }

    @Transactional(readOnly = true)
    public List<Card> findByDeck(Long deckId) {
        deckService.getById(deckId);
        return cardRepository.findByDeckIdOrderByIdAsc(deckId);
    }

    @Transactional(readOnly = true)
    public List<Card> search(Long deckId, String query) {
        String text = TextUtils.clean(query);
        if (text.isEmpty()) {
            return findByDeck(deckId);
        }
        if (text.length() > SEARCH_MAX_LENGTH) {
            text = text.substring(0, SEARCH_MAX_LENGTH);
        }
        deckService.getById(deckId);
        String pattern = "%" + escapeLike(text.toLowerCase(Locale.ROOT)) + "%";
        return cardRepository.searchInDeck(deckId, pattern);
    }

    static String escapeLike(String text) {
        return text.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }

    @Transactional(readOnly = true)
    public Card getCard(Long deckId, Long cardId) {
        return cardRepository.findByIdAndDeckId(cardId, deckId)
                .orElseThrow(() -> new NotFoundException("Карточка не найдена"));
    }

    @Transactional
    public Card addCard(Long deckId, String term, String definition) {
        Deck deck = deckService.getById(deckId);
        Card card = new Card(TextUtils.clean(term), TextUtils.clean(definition));
        deck.addCard(card);
        return cardRepository.save(card);
    }

    @Transactional
    public int addCards(Long deckId, List<ParsedCard> parsedCards) {
        Deck deck = deckService.getById(deckId);
        List<ParsedCard> newCards = withoutDuplicates(deckId, parsedCards);
        for (ParsedCard parsedCard : newCards) {
            Card card = new Card(TextUtils.clean(parsedCard.getTerm()), TextUtils.clean(parsedCard.getDefinition()));
            deck.addCard(card);
            cardRepository.save(card);
        }
        return newCards.size();
    }

    @Transactional(readOnly = true)
    public List<ParsedCard> withoutDuplicates(Long deckId, List<ParsedCard> parsedCards) {
        deckService.getById(deckId);
        Set<String> knownTerms = new HashSet<>();
        for (Card card : cardRepository.findByDeckIdOrderByIdAsc(deckId)) {
            knownTerms.add(termKey(card.getTerm()));
        }
        List<ParsedCard> result = new ArrayList<>();
        for (ParsedCard parsedCard : parsedCards) {
            if (knownTerms.add(termKey(parsedCard.getTerm()))) {
                result.add(parsedCard);
            }
        }
        return result;
    }

    static String termKey(String term) {
        return TextUtils.clean(term).toLowerCase(Locale.ROOT);
    }

    @Transactional
    public Card updateCard(Long deckId, Long cardId, String term, String definition) {
        Card card = getCard(deckId, cardId);
        card.setTerm(TextUtils.clean(term));
        card.setDefinition(TextUtils.clean(definition));
        return card;
    }

    @Transactional
    public void deleteCard(Long deckId, Long cardId) {
        Card card = getCard(deckId, cardId);
        card.getDeck().removeCard(card);
    }
}
