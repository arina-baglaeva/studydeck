package com.studydeck.repository;

import com.studydeck.model.Card;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByDeckIdOrderByIdAsc(Long deckId);

    Optional<Card> findByIdAndDeckId(Long id, Long deckId);

    long countByDeckId(Long deckId);

    @Query("select c from Card c where c.deck.id = :deckId "
            + "and (lower(c.term) like :pattern escape '!' "
            + "or lower(c.definition) like :pattern escape '!') "
            + "order by c.id asc")
    List<Card> searchInDeck(@Param("deckId") Long deckId, @Param("pattern") String pattern);

    @Query("select distinct c.deck.id from Card c where c.nextReviewAt <= :now order by c.deck.id")
    List<Long> findDeckIdsWithDueCards(@Param("now") LocalDateTime now);

    @Query("select distinct c.deck.id from Card c where c.nextReviewAt is null order by c.deck.id")
    List<Long> findDeckIdsWithNewCards();

    @EntityGraph(attributePaths = "deck")
    Optional<Card> findFirstByDeckIdAndNextReviewAtLessThanEqualOrderByNextReviewAtAscIdAsc(Long deckId, LocalDateTime now);

    @EntityGraph(attributePaths = "deck")
    Optional<Card> findFirstByDeckIdAndNextReviewAtIsNullOrderByIdAsc(Long deckId);

    @EntityGraph(attributePaths = "deck")
    Optional<Card> findWithDeckById(Long id);

    long countByNextReviewAtLessThanEqual(LocalDateTime now);

    long countByNextReviewAtIsNull();
}
