package com.studydeck.service;

import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Card;
import com.studydeck.model.Deck;
import com.studydeck.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CardService {

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
