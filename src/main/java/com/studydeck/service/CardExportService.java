package com.studydeck.service;

import com.studydeck.model.Card;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CardExportService {

    static final String HEADER = "term,definition";
    private static final String DANGEROUS_START = "=+-@\t\r";

    private final CardService cardService;

    public CardExportService(CardService cardService) {
        this.cardService = cardService;
    }

    @Transactional(readOnly = true)
    public String exportDeck(Long deckId) {
        return toCsv(cardService.findByDeck(deckId));
    }

    String toCsv(List<Card> cards) {
        StringBuilder csv = new StringBuilder();
        csv.append(HEADER).append("\r\n");
        for (Card card : cards) {
            csv.append(toCsvCell(card.getTerm()))
                    .append(',')
                    .append(toCsvCell(card.getDefinition()))
                    .append("\r\n");
        }
        return csv.toString();
    }

    static String toCsvCell(String value) {
        String text = value == null ? "" : value;
        if (startsDangerously(text)) {
            text = "'" + text;
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    static boolean startsDangerously(String text) {
        return !text.isEmpty() && DANGEROUS_START.indexOf(text.charAt(0)) >= 0;
    }
}
