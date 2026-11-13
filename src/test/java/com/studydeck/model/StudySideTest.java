package com.studydeck.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StudySideTest {

    private final Card card = new Card("JVM", "виртуальная машина Java");

    @Test
    void termSideShowsTermFirst() {
        assertThat(StudySide.TERM.front(card)).isEqualTo("JVM");
        assertThat(StudySide.TERM.back(card)).isEqualTo("виртуальная машина Java");
    }

    @Test
    void definitionSideShowsDefinitionFirst() {
        assertThat(StudySide.DEFINITION.front(card)).isEqualTo("виртуальная машина Java");
        assertThat(StudySide.DEFINITION.back(card)).isEqualTo("JVM");
    }

    @Test
    void labelsMatchSides() {
        assertThat(StudySide.TERM.getFrontLabel()).isEqualTo("Термин");
        assertThat(StudySide.TERM.getBackLabel()).isEqualTo("Определение");
        assertThat(StudySide.DEFINITION.getFrontLabel()).isEqualTo("Определение");
        assertThat(StudySide.DEFINITION.getBackLabel()).isEqualTo("Термин");
    }
}
