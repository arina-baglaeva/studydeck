package com.studydeck.service;

import com.studydeck.dto.RecallResult;
import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Card;
import com.studydeck.model.Grade;
import com.studydeck.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ReviewService {

    private final CardRepository cardRepository;
    private final SpacedRepetitionService spacedRepetitionService;
    private final Random random;

    @Autowired
    public ReviewService(CardRepository cardRepository, SpacedRepetitionService spacedRepetitionService) {
        this(cardRepository, spacedRepetitionService, new Random());
    }

    ReviewService(CardRepository cardRepository, SpacedRepetitionService spacedRepetitionService, Random random) {
        this.cardRepository = cardRepository;
        this.spacedRepetitionService = spacedRepetitionService;
        this.random = random;
    }

    @Transactional(readOnly = true)
    public Optional<Card> nextCard(LocalDateTime now) {
        List<Long> dueDeckIds = cardRepository.findDeckIdsWithDueCards(now);
        if (!dueDeckIds.isEmpty()) {
            Long deckId = randomDeckId(dueDeckIds);
            return cardRepository.findFirstByDeckIdAndNextReviewAtLessThanEqualOrderByNextReviewAtAscIdAsc(deckId, now);
        }
        List<Long> newDeckIds = cardRepository.findDeckIdsWithNewCards();
        if (!newDeckIds.isEmpty()) {
            Long deckId = randomDeckId(newDeckIds);
            return cardRepository.findFirstByDeckIdAndNextReviewAtIsNullOrderByIdAsc(deckId);
        }
        return Optional.empty();
    }

    private Long randomDeckId(List<Long> deckIds) {
        return deckIds.get(random.nextInt(deckIds.size()));
    }

    @Transactional(readOnly = true)
    public long countForToday(LocalDateTime now) {
        return cardRepository.countByNextReviewAtLessThanEqual(now) + cardRepository.countByNextReviewAtIsNull();
    }

    @Transactional(readOnly = true)
    public Optional<Card> findWaitingCard(Long cardId, LocalDateTime now) {
        Card card = cardRepository.findWithDeckById(cardId)
                .orElseThrow(() -> new NotFoundException("Карточка не найдена"));
        if (!isWaitingForReview(card, now)) {
            return Optional.empty();
        }
        return Optional.of(card);
    }

    public RecallResult checkRecall(Card card, String typedAnswer) {
        String typed = TextUtils.clean(typedAnswer);
        if (typed.length() > Card.DEFINITION_MAX_LENGTH) {
            typed = typed.substring(0, Card.DEFINITION_MAX_LENGTH);
        }
        if (typed.isEmpty()) {
            return RecallResult.notTyped();
        }
        boolean matches = TextUtils.normalize(typed).equals(TextUtils.normalize(card.getDefinition()));
        return new RecallResult(typed, matches);
    }

    @Transactional
    public boolean grade(Long cardId, Grade grade, LocalDateTime now) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карточка не найдена"));
        if (!isWaitingForReview(card, now)) {
            return false;
        }
        spacedRepetitionService.review(card, grade, now);
        return true;
    }

    static boolean isWaitingForReview(Card card, LocalDateTime now) {
        return card.isNew() || card.isDue(now);
    }
}
