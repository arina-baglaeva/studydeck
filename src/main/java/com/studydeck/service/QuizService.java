package com.studydeck.service;

import com.studydeck.dto.QuizAnswer;
import com.studydeck.dto.QuizQuestion;
import com.studydeck.dto.QuizResult;
import com.studydeck.dto.WriteQuestion;
import com.studydeck.model.Card;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
public class QuizService {

    public static final int OPTION_COUNT = 4;
    public static final int MAX_QUESTIONS = 10;

    private final CardService cardService;
    private final Random random = new Random();

    public QuizService(CardService cardService) {
        this.cardService = cardService;
    }

    public List<QuizQuestion> newQuiz(Long deckId) {
        List<Card> cards = new ArrayList<>(cardService.findByDeck(deckId));
        Collections.shuffle(cards, random);
        List<QuizQuestion> questions = new ArrayList<>();
        Set<String> askedTerms = new HashSet<>();
        for (Card card : cards) {
            if (questions.size() == MAX_QUESTIONS) {
                break;
            }
            List<String> wrongOptions = wrongOptionsFor(card, cards);
            if (wrongOptions.size() == OPTION_COUNT - 1 && askedTerms.add(TextUtils.normalize(card.getTerm()))) {
                questions.add(createQuestion(card, wrongOptions));
            }
        }
        return questions;
    }

    public List<WriteQuestion> newWriteQuiz(Long deckId) {
        List<Card> cards = new ArrayList<>(cardService.findByDeck(deckId));
        Collections.shuffle(cards, random);
        List<WriteQuestion> questions = new ArrayList<>();
        Set<String> askedDefinitions = new HashSet<>();
        for (Card card : cards) {
            if (questions.size() == MAX_QUESTIONS) {
                break;
            }
            if (askedDefinitions.add(TextUtils.normalize(card.getDefinition()))) {
                questions.add(new WriteQuestion(card.getId(), card.getDefinition(), card.getTerm()));
            }
        }
        return questions;
    }

    public QuizResult checkWritten(List<WriteQuestion> questions, List<String> typedAnswers) {
        List<QuizAnswer> answers = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            String typed = i < typedAnswers.size() ? typedAnswers.get(i) : null;
            answers.add(checkWrittenAnswer(questions.get(i), typed));
        }
        return new QuizResult(answers);
    }

    public static boolean isSameAnswer(String typed, String expected) {
        if (typed == null || typed.length() > Card.TERM_MAX_LENGTH) {
            return false;
        }
        String cleanTyped = TextUtils.normalize(typed);
        return !cleanTyped.isEmpty() && cleanTyped.equals(TextUtils.normalize(expected));
    }

    public QuizResult check(List<QuizQuestion> questions, List<Integer> chosenIndexes) {
        List<QuizAnswer> answers = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            Integer chosenIndex = i < chosenIndexes.size() ? chosenIndexes.get(i) : null;
            answers.add(checkAnswer(questions.get(i), chosenIndex));
        }
        return new QuizResult(answers);
    }

    private QuizAnswer checkAnswer(QuizQuestion question, Integer chosenIndex) {
        boolean validIndex = chosenIndex != null && chosenIndex >= 0 && chosenIndex < question.getOptions().size();
        if (!validIndex) {
            return new QuizAnswer(question.getTerm(), null, question.getCorrectAnswer(), false);
        }
        String chosenAnswer = question.getOptions().get(chosenIndex);
        return new QuizAnswer(question.getTerm(), chosenAnswer, question.getCorrectAnswer(),
                question.isCorrect(chosenIndex));
    }

    private QuizAnswer checkWrittenAnswer(WriteQuestion question, String typed) {
        String shownAnswer = TextUtils.cleanOrNull(typed);
        if (shownAnswer != null && shownAnswer.length() > Card.TERM_MAX_LENGTH) {
            shownAnswer = shownAnswer.substring(0, Card.TERM_MAX_LENGTH);
        }
        return new QuizAnswer(question.getPrompt(), shownAnswer, question.getAnswer(),
                isSameAnswer(typed, question.getAnswer()));
    }

    private QuizQuestion createQuestion(Card card, List<String> wrongOptions) {
        List<String> options = new ArrayList<>(wrongOptions);
        options.add(card.getDefinition());
        Collections.shuffle(options, random);
        int correctIndex = options.indexOf(card.getDefinition());
        return new QuizQuestion(card.getId(), card.getTerm(), options, correctIndex);
    }

    private List<String> wrongOptionsFor(Card card, List<Card> cards) {
        List<Card> others = new ArrayList<>(cards);
        Collections.shuffle(others, random);
        String term = TextUtils.normalize(card.getTerm());
        Set<String> usedDefinitions = new HashSet<>();
        usedDefinitions.add(TextUtils.normalize(card.getDefinition()));
        List<String> wrongOptions = new ArrayList<>();
        for (Card other : others) {
            if (wrongOptions.size() == OPTION_COUNT - 1) {
                break;
            }
            boolean sameTerm = TextUtils.normalize(other.getTerm()).equals(term);
            if (!sameTerm && usedDefinitions.add(TextUtils.normalize(other.getDefinition()))) {
                wrongOptions.add(other.getDefinition());
            }
        }
        return wrongOptions;
    }
}
