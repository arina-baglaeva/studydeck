package com.studydeck.service;

import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Deck;
import com.studydeck.repository.DeckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeckService {

    private final DeckRepository deckRepository;

    public DeckService(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    @Transactional(readOnly = true)
    public List<Deck> findAll() {
        return deckRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Deck getById(Long id) {
        return deckRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Колода не найдена"));
    }

    @Transactional
    public Deck create(String title, String description) {
        Deck deck = new Deck(TextUtils.clean(title), TextUtils.cleanOrNull(description));
        return deckRepository.save(deck);
    }

    @Transactional
    public Deck update(Long id, String title, String description) {
        Deck deck = getById(id);
        deck.setTitle(TextUtils.clean(title));
        deck.setDescription(TextUtils.cleanOrNull(description));
        return deck;
    }

    @Transactional
    public void delete(Long id) {
        Deck deck = getById(id);
        deckRepository.delete(deck);
    }
}
