package com.tw.coupang.one_payroll.common.exception;

import com.tw.coupang.one_payroll.common.dto.ApiErrorResponse;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeConflictException;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeInactiveException;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.integration.exception.BatchNotFoundException;
import com.tw.coupang.one_payroll.paygroups.exception.DuplicatePayGroupException;
import com.tw.coupang.one_payroll.paygroups.exception.PayGroupNotFoundException;
import com.tw.coupang.one_payroll.payslip.exception.PayslipNotFoundException;
import com.tw.coupang.one_payroll.payperiod.exception.OverlappingPayPeriodException;
import com.tw.coupang.one_payroll.payroll.dto.response.ApiResponse;
import com.tw.coupang.one_payroll.payperiod.exception.InvalidPayPeriodException;
import com.tw.coupang.one_payroll.userauth.exception.UserIdAlreadyExistsException;
import com.tw.coupang.one_payroll.userauth.exception.AuthenticationException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String VALIDATION_ERROR_CODE = "VALIDATION_ERROR";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleInvalidBody(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getBindingResult().getFieldErrors());

        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }

        ApiResponse response = ApiResponse.failure(
                VALIDATION_ERROR_CODE,
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
                VALIDATION_ERROR_CODE,
                "Validation failed for one or more fields.",
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BatchNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBatchNotFound(BatchNotFoundException ex) {
        log.warn("BatchNotFoundException caught: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleInvalidJson(HttpMessageNotReadableException ex) {
        String details = ex.getMostSpecificCause().getMessage();

        log.warn("Invalid JSON or enum value: {}", details);

        if (details.contains("not one of the values accepted")) {
            ApiResponse response = ApiResponse.failure(
                    VALIDATION_ERROR_CODE,
                    "Invalid value provided for an enum field.",
                    details
            );
            return ResponseEntity.badRequest().body(response);
        }

        if (details.contains("Invalid `null` value")) {
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

            ApiResponse response = ApiResponse.failure(
                    VALIDATION_ERROR_CODE,
                    userMessage,
                    detailMsg
            );

            return ResponseEntity.badRequest().body(response);
        }

        if (details.contains("Unexpected character") || details.contains("expected a value")) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.failure(
                            VALIDATION_ERROR_CODE,
                            "Malformed JSON. One or more fields have missing or invalid values.",
                            "A value was missing or incorrectly formatted in the request body. Please check the JSON structure and ensure all fields have valid values."
                    )
            );
        }

        ApiResponse response = ApiResponse.failure(
                VALIDATION_ERROR_CODE,
                "Invalid request format.",
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

    @ExceptionHandler(EmployeeInactiveException.class)
    public ResponseEntity<ApiResponse> handleInactiveEmployee(EmployeeInactiveException ex) {
        log.warn("Inactive employee: {}", ex.getMessage());

        ApiResponse response = ApiResponse.failure(
                "EMPLOYEE_INACTIVE",
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(OverlappingPayPeriodException.class)
    public ResponseEntity<ApiResponse> handleOverlappingPayPeriod(OverlappingPayPeriodException ex) {
        log.warn("Pay period overlap: {}", ex.getMessage());

        ApiResponse response = ApiResponse.failure(
                "PAY_PERIOD_OVERLAP",
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);

        ApiResponse response = ApiResponse.failure(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                null
        );

        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(PayslipNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePayslipNotFound(PayslipNotFoundException ex) {
        log.warn("Payslip not found: {}", ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.failure("INVALID_PAYSLIP", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state encountered: {}", ex.getMessage());

        if ("Payroll not ready".equals(ex.getMessage())) {
            ApiErrorResponse response = ApiErrorResponse.failure(
                    "INVALID_REQUEST", "Payroll is not ready yet for the requested employee and period.",
                    Map.of("reason", "PAYROLL_NOT_READY"));
            return ResponseEntity.badRequest().body(response);
        } else {
            ApiErrorResponse response = ApiErrorResponse.failure("INTERNAL_ERROR", ex.getMessage(),
                    Map.of("reason", "ILLEGAL_STATE"));
            return ResponseEntity.badRequest().body(response);
        }

    }

    @ExceptionHandler(UserIdAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleUserIdExists(UserIdAlreadyExistsException ex) {
        log.warn("UserId already exists: {}", ex.getMessage());

        ApiResponse response = ApiResponse.failure(
                "USERID_ALREADY_EXISTS",
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        ApiResponse response = ApiResponse.failure(
                "INVALID_CREDENTIALS",
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

}
