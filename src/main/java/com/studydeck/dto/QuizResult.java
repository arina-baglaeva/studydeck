package com.studydeck.dto;

import java.util.List;

public class QuizResult {

    private final List<QuizAnswer> answers;

    public QuizResult(List<QuizAnswer> answers) {
        this.answers = List.copyOf(answers);
    }

    public List<QuizAnswer> getAnswers() {
        return answers;
    }

    public int getTotal() {
        return answers.size();
    }

    public int getCorrectCount() {
        int count = 0;
        for (QuizAnswer answer : answers) {
            if (answer.isCorrect()) {
                count++;
            }
        }
        return count;
    }

    public int getPercent() {
        if (answers.isEmpty()) {
            return 0;
        }
        return getCorrectCount() * 100 / answers.size();
    }
}
