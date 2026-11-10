package com.studydeck.controller;

import com.studydeck.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String DEFAULT_NOT_FOUND_MESSAGE = "Страница не найдена";
    static final String TOO_LARGE_FILE_MESSAGE = "Файл слишком большой: можно загрузить не больше 1 МБ";

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NotFoundException exception, Model model) {
        model.addAttribute("errorMessage", exception.getMessage());
        return "error/404";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public String handleTooLargeFile(Model model) {
        model.addAttribute("status", HttpStatus.PAYLOAD_TOO_LARGE.value());
        model.addAttribute("errorMessage", TOO_LARGE_FILE_MESSAGE);
        return "error/4xx";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleWrongId(Model model) {
        model.addAttribute("errorMessage", DEFAULT_NOT_FOUND_MESSAGE);
        return "error/404";
    }
}
