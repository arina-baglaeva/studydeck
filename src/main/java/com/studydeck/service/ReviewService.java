package com.studydeck.service;

import com.studydeck.dto.RecallResult;
import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Card;
import com.studydeck.model.Grade;
import com.studydeck.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReviewService {

    private final CardRepository cardRepository;
    private final SpacedRepetitionService spacedRepetitionService;

    public ReviewService(CardRepository cardRepository, SpacedRepetitionService spacedRepetitionService) {
        this.cardRepository = cardRepository;
        this.spacedRepetitionService = spacedRepetitionService;
    }

    @Transactional(readOnly = true)
    public Optional<Card> nextCard(LocalDateTime now) {
        Optional<Card> dueCard = cardRepository.findFirstByNextReviewAtLessThanEqualOrderByNextReviewAtAscIdAsc(now);
        if (dueCard.isPresent()) {
            return dueCard;
        }
        return cardRepository.findFirstByNextReviewAtIsNullOrderByIdAsc();
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
