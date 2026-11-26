package com.studydeck.repository;

import com.studydeck.model.Deck;
import com.studydeck.model.StudyMode;
import com.studydeck.model.StudySession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StudySessionRepositoryTest {

    @Autowired
    private StudySessionRepository studySessionRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savesSessionWithFinishTime() {
        Deck deck = deckRepository.save(new Deck("Java", null));

        StudySession saved = studySessionRepository.save(new StudySession(deck, StudyMode.MATCH, 6, 6, 42_000L));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFinishedAt()).isNotNull();
        assertThat(saved.getDurationMillis()).isEqualTo(42_000L);
    }

    @Test
    void findsOnlySessionsOfGivenDeckNewestFirst() {
        Deck java = deckRepository.save(new Deck("Java", null));
        Deck english = deckRepository.save(new Deck("English", null));
        StudySession first = studySessionRepository.save(new StudySession(java, StudyMode.QUIZ, 3, 5, null));
        StudySession second = studySessionRepository.save(new StudySession(java, StudyMode.WRITE, 4, 5, null));
        studySessionRepository.save(new StudySession(english, StudyMode.QUIZ, 1, 5, null));

        assertThat(studySessionRepository.findByDeckIdOrderByFinishedAtDescIdDesc(java.getId()))
                .containsExactly(second, first);
        assertThat(studySessionRepository.countByDeckId(english.getId())).isEqualTo(1);
    }

    @Test
    void deletingDeckDeletesItsSessions() {
        Deck deck = deckRepository.save(new Deck("Java", null));
        studySessionRepository.saveAndFlush(new StudySession(deck, StudyMode.QUIZ, 3, 5, null));
        entityManager.clear();

        deckRepository.delete(deckRepository.findById(deck.getId()).orElseThrow());
        deckRepository.flush();
        entityManager.clear();

        assertThat(studySessionRepository.count()).isZero();
    }
}
