package com.studydeck.controller;

import com.studydeck.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String DEFAULT_NOT_FOUND_MESSAGE = "Страница не найдена";

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NotFoundException exception, Model model) {
        model.addAttribute("errorMessage", exception.getMessage());
        return "error/404";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleWrongId(Model model) {
        model.addAttribute("errorMessage", DEFAULT_NOT_FOUND_MESSAGE);
        return "error/404";
    }
}
