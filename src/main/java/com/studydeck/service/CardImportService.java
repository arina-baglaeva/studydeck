package com.studydeck.service;

import com.studydeck.dto.ParseResult;
import com.studydeck.exception.ImportException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
public class CardImportService {

    public static final long MAX_FILE_SIZE = 1024 * 1024;
    public static final int MAX_LINES = 2000;
    public static final List<String> ALLOWED_EXTENSIONS = List.of(".txt", ".csv");

    private static final String BOM = "﻿";

    private final CardFileParser parser;

    public CardImportService(CardFileParser parser) {
        this.parser = parser;
    }

    public ParseResult readFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImportException("Выберите непустой файл");
        }
        String fileName = file.getOriginalFilename();
        if (!hasAllowedExtension(fileName)) {
            throw new ImportException("Можно загрузить только файлы .txt или .csv");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ImportException("Файл слишком большой: максимум 1 МБ");
        }
        String text = readText(file);
        if (text.lines().count() > MAX_LINES) {
            throw new ImportException("Слишком много строк: максимум " + MAX_LINES);
        }
        return parser.parseFile(fileName, text);
    }

    boolean hasAllowedExtension(String fileName) {
        if (fileName == null) {
            return false;
        }
        String name = fileName.toLowerCase(Locale.ROOT);
        for (String extension : ALLOWED_EXTENSIONS) {
            if (name.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private String readText(MultipartFile file) {
        try {
            String text = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(file.getBytes()))
                    .toString();
            if (text.startsWith(BOM)) {
                text = text.substring(1);
            }
            return text;
        } catch (CharacterCodingException e) {
            throw new ImportException("Файл должен быть в кодировке UTF-8");
        } catch (IOException e) {
            throw new ImportException("Не удалось прочитать файл");
        }
    }
}
