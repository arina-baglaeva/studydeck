package com.studydeck.controller;

import com.studydeck.service.CardExportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.charset.StandardCharsets;

@Controller
public class ExportController {

    static final MediaType CSV_TYPE = new MediaType("text", "csv", StandardCharsets.UTF_8);
    private static final String BOM = "﻿";

    private final CardExportService cardExportService;

    public ExportController(CardExportService cardExportService) {
        this.cardExportService = cardExportService;
    }

    @GetMapping("/decks/{deckId}/export")
    public ResponseEntity<byte[]> exportCsv(@PathVariable Long deckId) {
        String csv = cardExportService.exportDeck(deckId);
        byte[] body = (BOM + csv).getBytes(StandardCharsets.UTF_8);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("deck-" + deckId + ".csv")
                .build();
        return ResponseEntity.ok()
                .contentType(CSV_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(body);
    }
}
