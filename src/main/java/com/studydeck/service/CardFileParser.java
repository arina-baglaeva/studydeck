package com.studydeck.service;

import com.studydeck.dto.ParseResult;
import com.studydeck.dto.ParsedCard;
import com.studydeck.model.Card;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class CardFileParser {

    private static final List<String> DELIMITERS = List.of("\t", " — ", " – ", " - ", "—", ":", ";");
    private static final List<String> HEADER_TERMS = List.of("term", "термин");

    public ParseResult parseFile(String fileName, String text) {
        if (fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            return parseCsv(text);
        }
        return parse(text);
    }

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
        return createCard(line.substring(0, index), line.substring(index + delimiter.length()));
    }

    ParsedCard createCard(String rawTerm, String rawDefinition) {
        String term = TextUtils.clean(rawTerm);
        String definition = TextUtils.clean(rawDefinition);
        if (term.isEmpty() || definition.isEmpty()) {
            return null;
        }
        if (term.length() > Card.TERM_MAX_LENGTH || definition.length() > Card.DEFINITION_MAX_LENGTH) {
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

    public ParseResult parseCsv(String text) {
        List<ParsedCard> cards = new ArrayList<>();
        List<Integer> badLines = new ArrayList<>();
        if (text == null) {
            return new ParseResult(cards, badLines);
        }
        String[] lines = text.split("\\R");
        char separator = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].isBlank()) {
                continue;
            }
            if (separator == 0) {
                separator = detectSeparator(lines[i]);
                if (isHeader(lines[i], separator)) {
                    continue;
                }
            }
            ParsedCard card = parseCsvLine(lines[i], separator);
            if (card == null) {
                badLines.add(i + 1);
            } else {
                cards.add(card);
            }
        }
        return new ParseResult(cards, badLines);
    }

    ParsedCard parseCsvLine(String line, char separator) {
        List<String> fields = splitCsvLine(line, separator);
        if (fields == null || fields.size() < 2) {
            return null;
        }
        return createCard(fields.get(0), fields.get(1));
    }

    List<String> splitCsvLine(String line, char separator) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else if (c == '"') {
                    inQuotes = false;
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == separator) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
            i++;
        }
        if (inQuotes) {
            return null;
        }
        fields.add(field.toString());
        return fields;
    }

    char detectSeparator(String line) {
        List<String> bySemicolon = splitCsvLine(line, ';');
        if (bySemicolon != null && bySemicolon.size() >= 2) {
            return ';';
        }
        return ',';
    }

    private boolean isHeader(String line, char separator) {
        List<String> fields = splitCsvLine(line, separator);
        if (fields == null || fields.isEmpty()) {
            return false;
        }
        String first = TextUtils.clean(fields.get(0)).toLowerCase(Locale.ROOT);
        return HEADER_TERMS.contains(first);
    }
}
