package com.tw.coupang.one_payroll.payroll.validator;

import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

@Slf4j
@Component
public class PayPeriodValidator implements ConstraintValidator<ValidPayPeriod, PayrollCalculationRequest> {

    @Override
    public boolean isValid(PayrollCalculationRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;
        if (request.getPayPeriod() == null) {
            addViolation(context, "payPeriod", "payPeriod must not be null");
            return false;
        }

        LocalDate start = request.getPayPeriod().getStartDate();
        LocalDate end = request.getPayPeriod().getEndDate();

        if (start == null || end == null) return true;

        log.debug("Validating pay period: start={}, end={}", start, end);

        if (!isEndAfterStart(start, end)) {
            log.warn("Invalid pay period: end is not after start (start={}, end={})", start, end);
            addViolation(context, "endDate", "endDate must be after startDate");
            return false;
        }

        if (!isSameMonth(start, end)) {
            log.warn("Invalid pay period: start and end not within same month (start={}, end={})", start, end);
            addViolation(context, "startDate", "period must be within a single calendar cycle (same month)");
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
                .addPropertyNode("payPeriod")
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
