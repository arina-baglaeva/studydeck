package com.studydeck.config;

import com.studydeck.model.Deck;
import com.studydeck.repository.DeckRepository;
import com.studydeck.service.CardService;
import com.studydeck.service.DeckService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DataLoader implements CommandLineRunner {

    private final DeckRepository deckRepository;
    private final DeckService deckService;
    private final CardService cardService;

    public DataLoader(DeckRepository deckRepository, DeckService deckService, CardService cardService) {
        this.deckRepository = deckRepository;
        this.deckService = deckService;
        this.cardService = cardService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (deckRepository.count() > 0) {
            return;
        }
        createEnglishDeck();
        createJavaDeck();
    }

    private void createEnglishDeck() {
        Deck deck = deckService.create("IT English", "Английские слова для программиста");
        cardService.addCard(deck.getId(), "bug", "ошибка в программе");
        cardService.addCard(deck.getId(), "deploy", "развернуть приложение на сервере");
        cardService.addCard(deck.getId(), "branch", "ветка в git");
        cardService.addCard(deck.getId(), "release", "выпуск новой версии");
    }

    private void createJavaDeck() {
        Deck deck = deckService.create("Java", "Основные термины Java");
        cardService.addCard(deck.getId(), "JVM", "Виртуальная машина Java, выполняет байт-код");
        cardService.addCard(deck.getId(), "JDK", "Набор инструментов для разработки на Java");
        cardService.addCard(deck.getId(), "JRE", "Среда выполнения Java-программ");
    }
}
