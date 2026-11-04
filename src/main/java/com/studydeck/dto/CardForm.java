package com.studydeck.dto;

import com.studydeck.model.Card;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CardForm {

    @NotBlank(message = "Введите термин")
    @Size(max = Card.TERM_MAX_LENGTH, message = "Термин не длиннее " + Card.TERM_MAX_LENGTH + " символов")
    private String term;

    @NotBlank(message = "Введите определение")
    @Size(max = Card.DEFINITION_MAX_LENGTH, message = "Определение не длиннее " + Card.DEFINITION_MAX_LENGTH + " символов")
    private String definition;

    public static CardForm fromCard(Card card) {
        CardForm form = new CardForm();
        form.setTerm(card.getTerm());
        form.setDefinition(card.getDefinition());
        return form;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
