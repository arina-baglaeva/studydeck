package com.studydeck.service;

import com.studydeck.dto.ParseResult;
import com.studydeck.dto.ParsedCard;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CardFileParser {

    private static final String DELIMITER = " - ";

    public ParseResult parse(String text) {
        List<ParsedCard> cards = new ArrayList<>();
        List<Integer> badLines = new ArrayList<>();
        if (text == null) {
            return new ParseResult(cards, badLines);
        }
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].isBlank()) {
                continue;
            }
            ParsedCard card = parseLine(lines[i]);
            if (card == null) {
                badLines.add(i + 1);
            } else {
                cards.add(card);
            }
        }
        return new ParseResult(cards, badLines);
    }

    ParsedCard parseLine(String line) {
        int index = line.indexOf(DELIMITER);
        if (index < 0) {
            return null;
        }
        String term = TextUtils.clean(line.substring(0, index));
        String definition = TextUtils.clean(line.substring(index + DELIMITER.length()));
        if (term.isEmpty() || definition.isEmpty()) {
            return null;
        }
        return new ParsedCard(term, definition);
    }
}
