package com.k955.Commune.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception, HttpServletRequest request
    ) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.NOT_FOUND,
                exception.getCode(),
                exception.getMessage(),
                request.getServletPath(),
                Instant.now());
        log.warn("Resource not found: code={}, path={}", exception.getCode(), request.getServletPath());
        return ResponseEntity.status(exceptionResponse.status()).body(exceptionResponse);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidRefreshTokenException(
            InvalidRefreshTokenException exception, HttpServletRequest request
    ) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST,
                exception.getCode(),
                exception.getMessage(),
                request.getServletPath(),
                Instant.now());
        log.warn("Resource not found: code={}, path={}", exception.getCode(), request.getServletPath());
        return ResponseEntity.status(exceptionResponse.status()).body(exceptionResponse);
    }

}
