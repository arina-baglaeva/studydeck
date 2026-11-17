package com.studydeck.dto;

public class QuizAnswer {

    private final String term;
    private final String chosenAnswer;
    private final String correctAnswer;
    private final boolean correct;

    public QuizAnswer(String term, String chosenAnswer, String correctAnswer, boolean correct) {
        this.term = term;
        this.chosenAnswer = chosenAnswer;
        this.correctAnswer = correctAnswer;
        this.correct = correct;
    }

    public String getTerm() {
        return term;
    }

    public String getChosenAnswer() {
        return chosenAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public boolean isAnswered() {
        return chosenAnswer != null;
    }
}
