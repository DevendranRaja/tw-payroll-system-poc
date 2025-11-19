package com.tw.coupang.one_payroll.common.exception;

import com.tw.coupang.one_payroll.EmployeeMaster.Exception.EmployeeConflictException;
import com.tw.coupang.one_payroll.paygroups.exception.DuplicatePayGroupException;
import com.tw.coupang.one_payroll.paygroups.exception.PayGroupNotFoundException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleInvalidBody(MethodArgumentNotValidException ex) {
        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(
                Map.of(
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "message", "Validation failed",
                        "errors", validationErrors
                )
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintValidation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv ->
                errors.put(cv.getPropertyPath().toString(), cv.getMessage())
        );

        return ResponseEntity.badRequest().body(
                Map.of(
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "message", "Validation failed",
                        "errors", errors
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleInvalidJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "message", "Invalid request format."
                )
        );
    }

    @ExceptionHandler(EmployeeConflictException.class)
    public ResponseEntity<?> handleEmployeeConflict(EmployeeConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                        "status", HttpStatus.CONFLICT.value(),
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(DuplicatePayGroupException.class)
    public ResponseEntity<?> handleDuplicatePayGroup(DuplicatePayGroupException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                        "status", HttpStatus.CONFLICT.value(),
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(PayGroupNotFoundException.class)
    public ResponseEntity<?> handlePayGroupNotFound(PayGroupNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "status", HttpStatus.NOT_FOUND.value(),
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {

        String msg = ex.getMessage() != null ? ex.getMessage() : "Invalid request";

        if (msg.toLowerCase().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("status", 404, "message", msg)
            );
        }

        return ResponseEntity.badRequest().body(
                Map.of("status", 400, "message", msg)
        );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.internalServerError().body(
                Map.of(
                        "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "message", "An unexpected error occurred. Please try again later."
                )
        );
    }

}
