package com.tw.coupang.one_payroll.common.exception;

import com.tw.coupang.one_payroll.EmployeeMaster.Exception.EmployeeConflictException;
import com.tw.coupang.one_payroll.EmployeeMaster.Exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.paygroups.exception.DuplicatePayGroupException;
import com.tw.coupang.one_payroll.paygroups.exception.PayGroupNotFoundException;
import com.tw.coupang.one_payroll.payroll.dto.response.ApiResponse;
import com.tw.coupang.one_payroll.payroll.exception.InvalidPayPeriodException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleInvalidBody(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getBindingResult().getFieldErrors());

        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }

        ApiResponse response = ApiResponse.failure(
                "VALIDATION_ERROR",
                "Validation failed for one or more fields.",
                validationErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintValidation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv ->
                errors.put(cv.getPropertyPath().toString(), cv.getMessage())
        );

        ApiResponse response = ApiResponse.failure(
                "VALIDATION_ERROR",
                "Validation failed for one or more fields.",
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleInvalidJson(HttpMessageNotReadableException ex) {
        String details = ex.getMostSpecificCause().getMessage();

        log.warn("Invalid JSON or enum value: {}", details);

        String message = "Invalid request format.";
        if (details != null) {
            if (details.contains("not one of the values accepted")) {
                message = "Invalid value provided for an enum field.";
            } else if (details.contains("Invalid `null` value")) {
                message = "One or more required fields cannot be null.";
            }
        }

        ApiResponse response = ApiResponse.failure(
                "VALIDATION_ERROR",
                message,
                details
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(EmployeeConflictException.class)
    public ResponseEntity<ApiResponse> handleEmployeeConflict(EmployeeConflictException ex) {
        log.warn("Employee conflict: {}", ex.getMessage());

        ApiResponse response = ApiResponse.failure("EMPLOYEE_CONFLICT", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiResponse> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        log.warn("Employee not found: {}", ex.getMessage());

        ApiResponse response = ApiResponse.failure("EMPLOYEE_NOT_FOUND", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DuplicatePayGroupException.class)
    public ResponseEntity<ApiResponse> handleDuplicate(DuplicatePayGroupException ex) {
        log.warn("Duplicate pay group error: {}", ex.getMessage());

        ApiResponse response = ApiResponse.failure("DUPLICATE_PAYGROUP", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(PayGroupNotFoundException.class)
    public ResponseEntity<ApiResponse> handlePayGroupNotFound(PayGroupNotFoundException ex) {
        log.warn("Pay group not found: {}", ex.getMessage());

        ApiResponse response = ApiResponse.failure("INVALID_PAYGROUP", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Invalid request";
        log.warn("IllegalArgumentException: {}", msg);

        ApiResponse response = ApiResponse.failure("INVALID_REQUEST", msg, ex);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(InvalidPayPeriodException.class)
    public ResponseEntity<ApiResponse> handleInvalidPayPeriod(InvalidPayPeriodException ex) {
        log.warn("Invalid pay period: {}", ex.getMessage());

        ApiResponse response = ApiResponse.failure(
                "INVALID_PAY_PERIOD",
                ex.getMessage(),
                null
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);

        ApiResponse response = ApiResponse.failure(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                ex
        );

        return ResponseEntity.internalServerError().body(response);
    }
}
