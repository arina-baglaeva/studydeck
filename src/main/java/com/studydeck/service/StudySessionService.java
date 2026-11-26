package com.studydeck.service;

import com.studydeck.model.Deck;
import com.studydeck.model.StudyMode;
import com.studydeck.model.StudySession;
import com.studydeck.repository.StudySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final DeckService deckService;

    public StudySessionService(StudySessionRepository studySessionRepository, DeckService deckService) {
        this.studySessionRepository = studySessionRepository;
        this.deckService = deckService;
    }

    @Transactional
    public StudySession recordAnswers(Long deckId, StudyMode mode, int correctCount, int totalCount) {
        return save(deckId, mode, correctCount, totalCount, null);
    }

    @Transactional
    public StudySession recordMatch(Long deckId, int pairCount, long timeMillis) {
        return save(deckId, StudyMode.MATCH, pairCount, pairCount, timeMillis);
    }

    @Transactional(readOnly = true)
    public List<StudySession> findByDeck(Long deckId) {
        return studySessionRepository.findByDeckIdOrderByFinishedAtDescIdDesc(deckId);
    }

    private StudySession save(Long deckId, StudyMode mode, int correctCount, int totalCount, Long durationMillis) {
        Deck deck = deckService.getById(deckId);
        StudySession session = new StudySession(deck, mode, correctCount, totalCount, durationMillis);
        return studySessionRepository.save(session);
    }
}
