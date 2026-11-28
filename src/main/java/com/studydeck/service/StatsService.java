package com.studydeck.service;

import com.studydeck.dto.DeckProgress;
import com.studydeck.dto.ModeStats;
import com.studydeck.dto.Streak;
import com.studydeck.model.StudyMode;
import com.studydeck.model.StudySession;
import com.studydeck.repository.DeckRepository;
import com.studydeck.repository.StudySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class StatsService {

    public static final int STREAK_LOOKBACK_DAYS = 366;

    private final StudySessionRepository studySessionRepository;
    private final DeckRepository deckRepository;

    public StatsService(StudySessionRepository studySessionRepository, DeckRepository deckRepository) {
        this.studySessionRepository = studySessionRepository;
        this.deckRepository = deckRepository;
    }

    @Transactional(readOnly = true)
    public List<ModeStats> modeStats() {
        Map<StudyMode, ModeStats> byMode = new EnumMap<>(StudyMode.class);
        for (ModeStats stats : studySessionRepository.findModeStats()) {
            byMode.put(stats.getMode(), stats);
        }
        List<ModeStats> result = new ArrayList<>();
        for (StudyMode mode : StudyMode.values()) {
            result.add(byMode.getOrDefault(mode, ModeStats.empty(mode)));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<StudySession> recentSessions() {
        return studySessionRepository.findTop10ByOrderByFinishedAtDescIdDesc();
    }

    @Transactional(readOnly = true)
    public long totalSessions() {
        return studySessionRepository.count();
    }

    @Transactional(readOnly = true)
    public List<DeckProgress> deckProgress() {
        return deckRepository.findAllProgress();
    }

    @Transactional(readOnly = true)
    public Streak currentStreak(LocalDate today) {
        LocalDateTime from = today.minusDays(STREAK_LOOKBACK_DAYS).atStartOfDay();
        Set<LocalDate> studyDays = new HashSet<>();
        for (LocalDateTime finishedAt : studySessionRepository.findFinishedAtSince(from)) {
            studyDays.add(finishedAt.toLocalDate());
        }
        return StreakCalculator.calculate(studyDays, today);
    }
}
