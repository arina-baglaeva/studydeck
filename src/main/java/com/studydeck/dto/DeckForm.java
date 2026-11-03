package com.studydeck.dto;

import com.studydeck.model.Deck;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DeckForm {

    @NotBlank(message = "Введите название колоды")
    @Size(max = Deck.TITLE_MAX_LENGTH, message = "Название не длиннее " + Deck.TITLE_MAX_LENGTH + " символов")
    private String title;

    @Size(max = Deck.DESCRIPTION_MAX_LENGTH, message = "Описание не длиннее " + Deck.DESCRIPTION_MAX_LENGTH + " символов")
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
