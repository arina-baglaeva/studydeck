package com.studydeck.repository;

import com.studydeck.dto.DeckSummary;
import com.studydeck.model.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    List<Deck> findAllByOrderByCreatedAtDesc();

    @Query("select new com.studydeck.dto.DeckSummary(d.id, d.title, d.description, count(c)) "
            + "from Deck d left join d.cards c "
            + "group by d.id, d.title, d.description, d.createdAt "
            + "order by d.createdAt desc, d.id desc")
    List<DeckSummary> findAllSummaries();
}
