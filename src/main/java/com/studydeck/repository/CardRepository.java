package com.studydeck.repository;

import com.studydeck.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByDeckIdOrderByIdAsc(Long deckId);

    Optional<Card> findByIdAndDeckId(Long id, Long deckId);

    long countByDeckId(Long deckId);
}
