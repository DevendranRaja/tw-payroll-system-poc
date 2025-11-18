package com.tw.coupang.one_payroll.error.validation;

import com.tw.coupang.one_payroll.error.enums.ErrorCode;
import com.tw.coupang.one_payroll.error.response.ErrorResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationManagerTest {
    @Test
    void missingMandatoryFieldShouldThrowValidationException() {
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ValidationManager.validateMandatoryField("Employee Master", "E001", Arrays.asList("ABC","E001","Finance",""))
        );

        ErrorResponse response = ex.getErrorResponse();
        assertEquals("Employee Master", response.moduleName());
        assertEquals("E001", response.employeeId());
        assertEquals(ErrorCode.MISSING_MANDATORY_FIELD.getErrorMessage(), response.errorMessage());
    }

    @Test
    void shouldPassWhenMandatoryFieldIsNotEmpty() {
        assertDoesNotThrow(() -> ValidationManager.validateMandatoryField("Employee Master", "E001", Arrays.asList("ABC","E001","Finance","1")));
    }

    @Test
    void invalidEmailShouldThrowValidationException() {
        String invalidEmail = "invalidEmail.com";

        ValidationException ex = assertThrows(
                ValidationException.class,
                () ->
            ValidationManager.validateEmail("Employee Master", "E001",invalidEmail)
        );

        ErrorResponse response = ex.getErrorResponse();
        assertEquals("Employee Master", response.moduleName());
        assertEquals("E001", response.employeeId());
        assertEquals(ErrorCode.INVALID_EMAIL.getErrorMessage(), response.errorMessage());
    }

    @Test
    void shouldPassForValidEmail() {
        String validEmail = "test@example.com";

        assertDoesNotThrow(() -> ValidationManager.validateEmail("Employee Master", "E001", validEmail));
    }

    @Test
    void futureDateShouldThrowValidationException()
    {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ValidationManager.validateDate("Employee Master", "E001", futureDate)
        );

        ErrorResponse response = ex.getErrorResponse();
        assertEquals("Employee Master", response.moduleName());
        assertEquals("E001", response.employeeId());
        assertEquals(ErrorCode.INVALID_DATE.getErrorMessage(), response.errorMessage());
    }

    @Test
    void nullDateShouldThrowValidationException()
    {
        LocalDate date = null;

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ValidationManager.validateDate("Employee Master", "E001", date)
        );

        ErrorResponse response = ex.getErrorResponse();
        assertEquals("Employee Master", response.moduleName());
        assertEquals("E001", response.employeeId());
        assertEquals(ErrorCode.INVALID_DATE.getErrorMessage(), response.errorMessage());
    }

    @Test
    void shouldPassForValidDate()
    {
        LocalDate date = LocalDate.of(2025, 3, 20);

        assertDoesNotThrow(() -> ValidationManager.validateDate("Employee Master", "E001", date));
    }

    @Test
    void startDateNullShouldThrowValidationException() {

        LocalDate startDate = null;
        LocalDate endDate = LocalDate.of(2025, 11, 1);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ValidationManager.validateWorkingHours("Payroll Calculation", "E001", startDate, endDate)
        );

        ErrorResponse response = ex.getErrorResponse();
        assertEquals("Payroll Calculation", response.moduleName());
        assertEquals("E001", response.employeeId());
        assertEquals(ErrorCode.MISSING_PAY_PERIOD.getErrorMessage(), response.errorMessage());
    }

    @Test
    void endDateNullShouldThrowValidationException() {

        LocalDate endDate = null;
        LocalDate startDate = LocalDate.of(2025, 11, 1);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ValidationManager.validateWorkingHours("Payroll Calculation", "E001", startDate, endDate)
        );

        ErrorResponse response = ex.getErrorResponse();
        assertEquals("Payroll Calculation", response.moduleName());
        assertEquals("E001", response.employeeId());
        assertEquals(ErrorCode.MISSING_PAY_PERIOD.getErrorMessage(), response.errorMessage());
    }

    @Test
    void negativeHoursShouldThrowValidationException() {

        LocalDate startDate = LocalDate.of(2025, 11, 5);
        LocalDate endDate = LocalDate.of(2025, 11, 1);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ValidationManager.validateWorkingHours("Payroll Calculation", "E001", startDate, endDate)
        );

        ErrorResponse response = ex.getErrorResponse();
        assertEquals("Payroll Calculation", response.moduleName());
        assertEquals("E001", response.employeeId());
        assertEquals(ErrorCode.NEGATIVE_WORKING_HOURS.getErrorMessage(), response.errorMessage());
    }

    @Test
    void shouldPassForValidWorkingHours() {

        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 11, 5);

        assertDoesNotThrow(() -> ValidationManager.validateWorkingHours("Payroll Calculation", "E001", startDate, endDate));
    }

    @Test
    void negativeBigDecimalValuesShouldThrowValidationException()
    {
        String fieldName = "Gross Pay";
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ValidationManager.validateNonNegativeBigDecimalValues("Payroll Calculation", "E001", new BigDecimal("-10"), fieldName)
        );

        ErrorResponse response = ex.getErrorResponse();
        assertEquals("Payroll Calculation", response.moduleName());
        assertEquals("E001", response.employeeId());
        assertEquals(ErrorCode.NEGATIVE_VALUE.getErrorMessage() + fieldName, response.errorMessage());
    }

    @Test
    void shouldPassForZeroOrPositiveBigDecimal() {
        String fieldName = "Tax Deduction";
        assertDoesNotThrow(() -> ValidationManager.validateNonNegativeBigDecimalValues("Payroll Calculation", "E001", new BigDecimal("100.50"), fieldName));
        assertDoesNotThrow(() -> ValidationManager.validateNonNegativeBigDecimalValues("Payroll Calculation", "E001", BigDecimal.ZERO, fieldName));

    }

    @Test
    void invalidEnumValueShouldThrowValidationException()
    {
        String fieldName = "Error Code";

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ValidationManager.validateEnum("Error Validation", "E001", ErrorCode.class, "INVALID_ENUM", fieldName)
        );

        ErrorResponse response = ex.getErrorResponse();
        assertEquals("Error Validation", response.moduleName());
        assertEquals("E001", response.employeeId());
        assertEquals(ErrorCode.INVALID_VALUE.getErrorMessage() + fieldName, response.errorMessage());
    }

    @Test
    void shouldPassForValidEnum()
    {
        String fieldName = "Error Code";
        assertDoesNotThrow(() -> ValidationManager.validateEnum("Error Validation", "E001", ErrorCode.class, "INVALID_DATE",fieldName));
    }

}
