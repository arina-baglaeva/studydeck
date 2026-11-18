package com.studydeck.dto;

import java.io.Serializable;

public class WriteQuestion implements Serializable {

    private final Long cardId;
    private final String prompt;
    private final String answer;

    public WriteQuestion(Long cardId, String prompt, String answer) {
        this.cardId = cardId;
        this.prompt = prompt;
        this.answer = answer;
    }

    public Long getCardId() {
        return cardId;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getAnswer() {
        return answer;
    }
}
