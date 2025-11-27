package com.tw.coupang.one_payroll.common.exception;

import com.tw.coupang.one_payroll.EmployeeMaster.Exception.EmployeeConflictException;
import com.tw.coupang.one_payroll.EmployeeMaster.Exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.paygroups.exception.DuplicatePayGroupException;
import com.tw.coupang.one_payroll.paygroups.exception.PayGroupNotFoundException;
import com.tw.coupang.one_payroll.payslipEss.exception.PayslipNotFoundException;
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
    public ResponseEntity<?> handleInvalidBody(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getBindingResult().getFieldErrors());

        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(
                Map.of(
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "message", "Validation failed for one or more fields.",
                        "errors", validationErrors
                )
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintValidation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv ->
                errors.put(cv.getPropertyPath().toString(), cv.getMessage())
        );

        return ResponseEntity.badRequest().body(
                Map.of(
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "message", "Validation failed for one or more fields.",
                        "errors", errors
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleInvalidJson(HttpMessageNotReadableException ex) {
        String details = ex.getMostSpecificCause().getMessage();

        log.warn("Invalid JSON or enum value: {}", details);

        if (details.contains("not one of the values accepted")) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", HttpStatus.BAD_REQUEST.value(),
                            "message", "Invalid value provided for an enum field.",
                            "details", details
                    )
            );
        }

        if (details != null && details.contains("Invalid `null` value")) {
            String fieldName = null;

            if (details.contains("property")) {
                int start = details.indexOf("property \"") + 10;
                int end = details.indexOf("\"", start);
                fieldName = details.substring(start, end);
            }

            String userMessage = (fieldName != null)
                    ? "Field '" + fieldName + "' cannot be empty. Please provide a value."
                    : "One or more required fields cannot be empty.";

            String detailMsg = (fieldName != null)
                    ? fieldName + " received empty"
                    : "Null value encountered in request body";

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", HttpStatus.BAD_REQUEST.value(),
                            "message", userMessage,
                            "details", detailMsg
                    )
            );
        }

        return ResponseEntity.badRequest().body(
                Map.of(
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "message", "Invalid request format.",
                        "details", details
                )
        );
    }

    @ExceptionHandler(EmployeeConflictException.class)
    public ResponseEntity<?> handleEmployeeConflict(EmployeeConflictException ex) {
        log.warn("Employee conflict: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                        "status", HttpStatus.CONFLICT.value(),
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<?> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        log.warn("Employee not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "status", HttpStatus.NOT_FOUND.value(),
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(DuplicatePayGroupException.class)
    public ResponseEntity<?> handleDuplicate(DuplicatePayGroupException ex) {
        log.warn("Duplicate pay group error: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                        "status", HttpStatus.CONFLICT.value(),
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(PayGroupNotFoundException.class)
    public ResponseEntity<?> handlePayGroupNotFound(PayGroupNotFoundException ex) {
        log.warn("Pay group not found: {}", ex.getMessage());

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
        log.warn("IllegalArgumentException: {}", msg);

        if (msg.toLowerCase().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("status", HttpStatus.NOT_FOUND.value(), "message", msg)
            );
        }

        return ResponseEntity.badRequest().body(
                Map.of("status", HttpStatus.BAD_REQUEST.value(), "message", msg)
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

    @ExceptionHandler(PayslipNotFoundException.class)
    public ResponseEntity<?> handlePayslipnotFound(PayslipNotFoundException ex) {
        log.warn("Payslip not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "status", HttpStatus.NOT_FOUND.value(),
                        "message", ex.getMessage()
                )
        );
    }

}
