package com.tw.coupang.one_payroll.payroll.validator;

import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayPeriodValidatorTest {

    @InjectMocks
    private PayPeriodValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @Test
    void validWhenEndAfterStartAndSameMonth() {
        PayrollCalculationRequest req = PayrollCalculationRequest.builder()
                .employeeId("EMP100")
                .periodStart(LocalDate.of(2025, 12, 10))
                .periodEnd(LocalDate.of(2025, 12, 20))
                .hoursWorked(40)
                .build();

        assertTrue(validator.isValid(req, context));
    }

    @Test
    void invalidWhenEndEqualsStart() {
        mockViolationFlow();

        PayrollCalculationRequest req = PayrollCalculationRequest.builder()
                .employeeId("EMP200")
                .periodStart(LocalDate.of(2025, 12, 10))
                .periodEnd(LocalDate.of(2025, 12, 10))
                .hoursWorked(40)
                .build();

        assertFalse(validator.isValid(req, context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("periodEnd must be after periodStart");
    }

    @Test
    void invalidWhenEndBeforeStart() {
        mockViolationFlow();

        PayrollCalculationRequest req = PayrollCalculationRequest.builder()
                .employeeId("EMP300")
                .periodStart(LocalDate.of(2025, 12, 10))
                .periodEnd(LocalDate.of(2025, 12, 5))
                .hoursWorked(40)
                .build();

        assertFalse(validator.isValid(req, context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("periodEnd must be after periodStart");
    }

    @Test
    void invalidWhenDifferentMonths() {
        mockViolationFlow();

        PayrollCalculationRequest req = PayrollCalculationRequest.builder()
                .employeeId("EMP400")
                .periodStart(LocalDate.of(2025, 12, 28))
                .periodEnd(LocalDate.of(2026, 1, 3))
                .hoursWorked(40)
                .build();

        assertFalse(validator.isValid(req, context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("period must be within a single calendar cycle (same month)");
    }

    @Test
    void validWhenRequestIsNull() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void validWhenPeriodStartIsNull() {
        PayrollCalculationRequest request = PayrollCalculationRequest.builder()
                .periodEnd(LocalDate.now())
                .hoursWorked(10)
                .build();

        assertTrue(validator.isValid(request, context));
    }

    @Test
    void validWhenPeriodEndIsNull() {
        PayrollCalculationRequest request = PayrollCalculationRequest.builder()
                .periodStart(LocalDate.now())
                .hoursWorked(10)
                .build();

        assertTrue(validator.isValid(request, context));
    }

    private void mockViolationFlow() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        when(nodeBuilder.addConstraintViolation()).thenReturn(context);
    }
}
