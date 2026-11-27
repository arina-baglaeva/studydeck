package com.studydeck.repository;

import com.studydeck.dto.ModeStats;
import com.studydeck.model.StudySession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    List<StudySession> findByDeckIdOrderByFinishedAtDescIdDesc(Long deckId);

    long countByDeckId(Long deckId);

    @EntityGraph(attributePaths = "deck")
    List<StudySession> findTop10ByOrderByFinishedAtDescIdDesc();

    @Query("select new com.studydeck.dto.ModeStats(s.mode, count(s), sum(s.correctCount), sum(s.totalCount)) "
            + "from StudySession s group by s.mode")
    List<ModeStats> findModeStats();
}
