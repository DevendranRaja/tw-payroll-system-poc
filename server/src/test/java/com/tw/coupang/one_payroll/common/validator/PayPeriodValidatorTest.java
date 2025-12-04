package com.tw.coupang.one_payroll.common.validator;

import com.tw.coupang.one_payroll.payperiod.dto.request.PayPeriod;
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
                .payPeriod(
                        PayPeriod.builder()
                                .startDate(LocalDate.of(2025, 12, 10))
                                .endDate(LocalDate.of(2025, 12, 20))
                                .build()
                )
                .build();

        assertTrue(validator.isValid(req, context));
    }

    @Test
    void invalidWhenEndEqualsStart() {
        mockViolationFlow();

        PayrollCalculationRequest req = PayrollCalculationRequest.builder()
                .employeeId("EMP200")
                .payPeriod(
                        PayPeriod.builder()
                                .startDate(LocalDate.of(2025, 12, 10))
                                .endDate(LocalDate.of(2025, 12, 10))
                                .build()
                )
                .build();

        assertFalse(validator.isValid(req, context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("endDate must be after startDate");
    }

    @Test
    void invalidWhenEndBeforeStart() {
        mockViolationFlow();

        PayrollCalculationRequest req = PayrollCalculationRequest.builder()
                .employeeId("EMP300")
                .payPeriod(
                        PayPeriod.builder()
                                .startDate(LocalDate.of(2025, 12, 10))
                                .endDate(LocalDate.of(2025, 12, 5))
                                .build()
                )
                .build();

        assertFalse(validator.isValid(req, context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("endDate must be after startDate");
    }

    @Test
    void invalidWhenDifferentMonths() {
        mockViolationFlow();

        PayrollCalculationRequest req = PayrollCalculationRequest.builder()
                .employeeId("EMP400")
                .payPeriod(
                        PayPeriod.builder()
                                .startDate(LocalDate.of(2025, 12, 28))
                                .endDate(LocalDate.of(2026, 1, 3))
                                .build()
                )
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
                .payPeriod(
                        PayPeriod.builder()
                                .endDate(LocalDate.now())
                                .build()
                )
                .build();

        assertTrue(validator.isValid(request, context));
    }

    @Test
    void validWhenPayPeriodEndDateIsNull() {
        PayrollCalculationRequest request = PayrollCalculationRequest.builder()
                .payPeriod(
                        PayPeriod.builder()
                                .startDate(LocalDate.now())
                                .build()
                )
                .build();

        assertTrue(validator.isValid(request, context));
    }

    private void mockViolationFlow() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        when(nodeBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        when(nodeBuilder.addConstraintViolation()).thenReturn(context);
    }
}
