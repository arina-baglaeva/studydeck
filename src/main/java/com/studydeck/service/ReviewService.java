package com.studydeck.service;

import com.studydeck.dto.RecallResult;
import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Card;
import com.studydeck.model.Grade;
import com.studydeck.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ReviewService {

    public static final int DEFAULT_NEW_CARDS_PER_DAY = 20;
    public static final int MAX_NEW_CARDS_PER_DAY = 1000;

    private final CardRepository cardRepository;
    private final SpacedRepetitionService spacedRepetitionService;
    private final Random random;
    private final int newCardsPerDay;

    @Autowired
    public ReviewService(CardRepository cardRepository,
                         SpacedRepetitionService spacedRepetitionService,
                         @Value("${studydeck.review.new-cards-per-day:20}") int newCardsPerDay) {
        this(cardRepository, spacedRepetitionService, new Random(), newCardsPerDay);
    }

    ReviewService(CardRepository cardRepository,
                  SpacedRepetitionService spacedRepetitionService,
                  Random random,
                  int newCardsPerDay) {
        if (newCardsPerDay < 0 || newCardsPerDay > MAX_NEW_CARDS_PER_DAY) {
            throw new IllegalArgumentException("newCardsPerDay must be between 0 and " + MAX_NEW_CARDS_PER_DAY);
        }
        this.cardRepository = cardRepository;
        this.spacedRepetitionService = spacedRepetitionService;
        this.random = random;
        this.newCardsPerDay = newCardsPerDay;
    }

    @Transactional(readOnly = true)
    public Optional<Card> nextCard(LocalDateTime now) {
        List<Long> dueDeckIds = cardRepository.findDeckIdsWithDueCards(now);
        if (!dueDeckIds.isEmpty()) {
            Long deckId = randomDeckId(dueDeckIds);
            return cardRepository.findFirstByDeckIdAndNextReviewAtLessThanEqualOrderByNextReviewAtAscIdAsc(deckId, now);
        }
        if (newCardsLeftToday(now) == 0) {
            return Optional.empty();
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
        long newCards = Math.min(cardRepository.countByNextReviewAtIsNull(), newCardsLeftToday(now));
        return cardRepository.countByNextReviewAtLessThanEqual(now) + newCards;
    }

    @Transactional(readOnly = true)
    public long newCardsLeftToday(LocalDateTime now) {
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        long startedToday = cardRepository.countByFirstReviewedAtGreaterThanEqual(startOfDay);
        return Math.max(0, newCardsPerDay - startedToday);
    }

    @Transactional(readOnly = true)
    public boolean hasNewCardsWaitingForTomorrow(LocalDateTime now) {
        return newCardsLeftToday(now) == 0 && cardRepository.countByNextReviewAtIsNull() > 0;
    }

    public int getNewCardsPerDay() {
        return newCardsPerDay;
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

    private boolean isWaitingForReview(Card card, LocalDateTime now) {
        if (card.isNew()) {
            return newCardsLeftToday(now) > 0;
        }
        return card.isDue(now);
    }
}
