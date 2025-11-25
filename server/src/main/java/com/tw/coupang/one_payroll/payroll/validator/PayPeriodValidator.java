package com.tw.coupang.one_payroll.payroll.validator;

import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

@Component
public class PayPeriodValidator implements ConstraintValidator<ValidPayPeriod, PayrollCalculationRequest> {

    @Override
    public boolean isValid(PayrollCalculationRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;

        LocalDate start = request.getPeriodStart();
        LocalDate end = request.getPeriodEnd();

        if (start == null || end == null) return true;

        if (!isEndAfterStart(start, end)) {
            addViolation(context, "periodEnd", "periodEnd must be after periodStart");
            return false;
        }

        if (!isSameMonth(start, end)) {
            addViolation(context, "periodStart", "period must be within a single calendar cycle (same month)");
            return false;
        }

        return true;
    }

    private boolean isEndAfterStart(LocalDate start, LocalDate end) {
        return end.isAfter(start);
    }

    private boolean isSameMonth(LocalDate start, LocalDate end) {
        return YearMonth.from(start).equals(YearMonth.from(end));
    }

    private void addViolation(ConstraintValidatorContext context, String field, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
