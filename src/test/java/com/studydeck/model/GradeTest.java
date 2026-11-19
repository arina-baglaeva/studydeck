package com.studydeck.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GradeTest {

    @Test
    void gradesGoFromWorstToBest() {
        assertThat(Grade.values()).containsExactly(Grade.AGAIN, Grade.HARD, Grade.GOOD, Grade.EASY);
    }

    @Test
    void labelsAreInRussian() {
        assertThat(Grade.AGAIN.getLabel()).isEqualTo("Снова");
        assertThat(Grade.HARD.getLabel()).isEqualTo("Трудно");
        assertThat(Grade.GOOD.getLabel()).isEqualTo("Хорошо");
        assertThat(Grade.EASY.getLabel()).isEqualTo("Легко");
    }

    @Test
    void qualityGrowsWithGrade() {
        assertThat(Grade.AGAIN.getQuality()).isLessThan(Grade.HARD.getQuality());
        assertThat(Grade.HARD.getQuality()).isLessThan(Grade.GOOD.getQuality());
        assertThat(Grade.GOOD.getQuality()).isLessThan(Grade.EASY.getQuality());
        assertThat(Grade.EASY.getQuality()).isEqualTo(5);
    }

    @Test
    void onlyAgainIsLapse() {
        assertThat(Grade.AGAIN.isLapse()).isTrue();
        assertThat(Grade.HARD.isLapse()).isFalse();
        assertThat(Grade.GOOD.isLapse()).isFalse();
        assertThat(Grade.EASY.isLapse()).isFalse();
    }
}
