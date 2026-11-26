package com.studydeck.repository;

import com.studydeck.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    List<StudySession> findByDeckIdOrderByFinishedAtDescIdDesc(Long deckId);

    long countByDeckId(Long deckId);
}
