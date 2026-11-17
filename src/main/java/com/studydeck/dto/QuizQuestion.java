package com.studydeck.dto;

import java.io.Serializable;
import java.util.List;

public class QuizQuestion implements Serializable {

    private final Long cardId;
    private final String term;
    private final List<String> options;
    private final int correctIndex;

    public QuizQuestion(Long cardId, String term, List<String> options, int correctIndex) {
        this.cardId = cardId;
        this.term = term;
        this.options = List.copyOf(options);
        this.correctIndex = correctIndex;
    }

    public boolean isCorrect(int optionIndex) {
        return optionIndex == correctIndex;
    }

    public Long getCardId() {
        return cardId;
    }

    public String getTerm() {
        return term;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public String getCorrectAnswer() {
        return options.get(correctIndex);
    }
}
