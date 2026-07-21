package com.uxelf.dev.ticketDealer.exception;

import com.uxelf.dev.ticketDealer.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception){

        Map<String,String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(
                (error)-> errors.put(error.getField(),error.getDefaultMessage())
        );

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrors(errors);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception){

        Map<String,String> errors = new HashMap<>();
        errors.put("error", exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrors(errors);

        return ResponseEntity.badRequest().body(errorResponse);
    }
}
