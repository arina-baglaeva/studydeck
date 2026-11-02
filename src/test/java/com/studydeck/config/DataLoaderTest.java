package com.studydeck.config;

import com.studydeck.repository.CardRepository;
import com.studydeck.repository.DeckRepository;
import com.studydeck.service.CardService;
import com.studydeck.service.DeckService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({DeckService.class, CardService.class, DataLoader.class})
class DataLoaderTest {

    @Autowired
    private DataLoader dataLoader;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private CardRepository cardRepository;

    @Test
    void loadsSampleDecksIntoEmptyDatabase() {
        dataLoader.run();

        assertThat(deckRepository.count()).isEqualTo(2);
        assertThat(cardRepository.count()).isEqualTo(7);
    }

    @Test
    void doesNotDuplicateDataOnSecondRun() {
        dataLoader.run();
        dataLoader.run();

        assertThat(deckRepository.count()).isEqualTo(2);
    }
}
