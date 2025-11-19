package com.tw.coupang.one_payroll.common.exception;

import com.tw.coupang.one_payroll.paygroups.exception.DuplicatePayGroupException;
import com.tw.coupang.one_payroll.paygroups.exception.PayGroupNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getBindingResult().getFieldErrors());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

        Map<String, Object> response = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", "Validation failed for one or more fields.",
                "errors", fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DuplicatePayGroupException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicatePayGroupException ex) {
        log.warn("Duplicate pay group error: {}", ex.getMessage());

        Map<String, Object> response = Map.of(
                "status", HttpStatus.CONFLICT.value(),
                "message", ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleInvalidJson(HttpMessageNotReadableException ex) {
        log.warn("Invalid JSON request: {}", ex.getMostSpecificCause().getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "message", "Invalid request format.",
                        "details", ex.getMostSpecificCause().getMessage()
                ));
    }

    @ExceptionHandler(PayGroupNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePayGroupNotFound(PayGroupNotFoundException ex) {
        log.warn("Pay group not found: {}", ex.getMessage());

        Map<String, Object> response = Map.of(
                "status", HttpStatus.NOT_FOUND.value(),
                "message", ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error occurred", ex);

        Map<String, Object> response = Map.of(
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "message", "An unexpected error occurred. Please try again later."
        );

        return ResponseEntity.internalServerError().body(response);
    }
}
