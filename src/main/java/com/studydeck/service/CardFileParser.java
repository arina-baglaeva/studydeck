package com.studydeck.service;

import com.studydeck.dto.ParseResult;
import com.studydeck.dto.ParsedCard;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CardFileParser {

    private static final List<String> DELIMITERS = List.of("\t", " — ", " – ", " - ", "—", ":", ";");

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
        String delimiter = findDelimiter(line);
        if (delimiter == null) {
            return null;
        }
        int index = line.indexOf(delimiter);
        String term = TextUtils.clean(line.substring(0, index));
        String definition = TextUtils.clean(line.substring(index + delimiter.length()));
        if (term.isEmpty() || definition.isEmpty()) {
            return null;
        }
        return new ParsedCard(term, definition);
    }

    String findDelimiter(String line) {
        for (String delimiter : DELIMITERS) {
            if (line.contains(delimiter)) {
                return delimiter;
            }
        }
        return null;
    }
}
