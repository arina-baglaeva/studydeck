package com.studydeck.service;

import com.studydeck.dto.DeckSummary;
import com.studydeck.dto.MatchResult;
import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Deck;
import com.studydeck.repository.DeckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    public List<DeckSummary> findAllSummaries() {
        return deckRepository.findAllSummaries();
    }

    @Transactional(readOnly = true)
    public Deck getById(Long id) {
        return deckRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Колода не найдена"));
    }

    @Transactional
    public Deck create(String title, String description) {
        return create(title, description, null);
    }

    @Transactional
    public Deck create(String title, String description, LocalDate examDate) {
        Deck deck = new Deck(TextUtils.clean(title), TextUtils.cleanOrNull(description));
        deck.setExamDate(examDate);
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
    public MatchResult recordMatchTime(Long id, long timeMillis) {
        Deck deck = getById(id);
        boolean newRecord = deck.recordMatchTime(timeMillis);
        return new MatchResult(deck.getBestMatchTimeMillis(), newRecord);
    }

    @Transactional
    public void delete(Long id) {
        Deck deck = getById(id);
        deckRepository.delete(deck);
    }
}
