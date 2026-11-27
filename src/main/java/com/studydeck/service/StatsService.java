package com.studydeck.service;

import com.studydeck.dto.ModeStats;
import com.studydeck.model.StudyMode;
import com.studydeck.model.StudySession;
import com.studydeck.repository.StudySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final StudySessionRepository studySessionRepository;

    public StatsService(StudySessionRepository studySessionRepository) {
        this.studySessionRepository = studySessionRepository;
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
}
