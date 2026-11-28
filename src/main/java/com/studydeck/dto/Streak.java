package com.studydeck.dto;

public class Streak {

    private final int days;
    private final boolean studiedToday;

    public Streak(int days, boolean studiedToday) {
        if (days < 0) {
            throw new IllegalArgumentException("Серия не может быть отрицательной");
        }
        this.days = days;
        this.studiedToday = studiedToday;
    }

    public int getDays() {
        return days;
    }

    public boolean isStudiedToday() {
        return studiedToday;
    }

    public String getDayWord() {
        int lastTwo = days % 100;
        int last = days % 10;
        if (lastTwo >= 11 && lastTwo <= 14) {
            return "дней";
        }
        if (last == 1) {
            return "день";
        }
        if (last >= 2 && last <= 4) {
            return "дня";
        }
        return "дней";
    }
}
