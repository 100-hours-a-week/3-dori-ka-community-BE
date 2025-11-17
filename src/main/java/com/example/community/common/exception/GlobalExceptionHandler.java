package com.example.community.common.exception;

import com.example.community.common.exception.custom.*;
import com.example.community.common.response.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.fail(message));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<APIResponse<?>> handleMethodNotAllowedException(HttpRequestMethodNotSupportedException e) {
        String message = e.getMessage();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(APIResponse.fail(message));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<APIResponse<?>> handleException(CustomException e) {
        ErrorMessage errorMessage = e.getErrorMessage();
        return ResponseEntity.status(errorMessage.getStatus()).body(APIResponse.fail(errorMessage.getMessage()));
    }
    
}
