package com.studydeck.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImportPreviewTest {

    private final ParsedCard bug = new ParsedCard("bug", "ошибка");
    private final ParsedCard fix = new ParsedCard("fix", "исправление");
    private final ImportPreview preview = new ImportPreview(1L, List.of(bug, fix), List.of(), 0);

    @Test
    void selectsCardsByIndexInOrder() {
        assertThat(preview.selectCards(List.of(1, 0))).containsExactly(bug, fix);
    }

    @Test
    void ignoresWrongAndRepeatedIndexes() {
        assertThat(preview.selectCards(Arrays.asList(-1, 5, null, 1, 1))).containsExactly(fix);
        assertThat(preview.selectCards(null)).isEmpty();
    }

    @Test
    void belongsOnlyToItsDeck() {
        assertThat(preview.belongsTo(1L)).isTrue();
        assertThat(preview.belongsTo(2L)).isFalse();
        assertThat(preview.belongsTo(null)).isFalse();
    }
}
