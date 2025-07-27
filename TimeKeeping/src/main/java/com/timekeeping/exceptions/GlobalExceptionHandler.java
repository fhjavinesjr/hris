package com.timekeeping.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDuplicateKeyException(DataIntegrityViolationException ex) {
        String errorMessage = ex.getMessage();

        //can use this code for specific error prompt message
        if(errorMessage.contains("duplicate")) {
            Pattern pattern = Pattern.compile("The duplicate key value is \\(.*?\\)");
            Matcher matcher = pattern.matcher(errorMessage);

            if (matcher.find()) {
                errorMessage = "Error found: " + matcher.group();
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }
}
