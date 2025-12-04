package com.tw.coupang.one_payroll.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

@Slf4j
@Component
public class PayPeriodValidator implements ConstraintValidator<ValidPayPeriod, HasPayPeriod> {

    @Override
    public boolean isValid(HasPayPeriod request, ConstraintValidatorContext context) {
        if (request == null || request.getPayPeriod() == null) {
            log.warn("PayPeriod validation skipped: request or payPeriod is null");
            return true;
        }

        final LocalDate start = request.getPayPeriod().getStartDate();
        final LocalDate end = request.getPayPeriod().getEndDate();

        if (start == null || end == null) {
            log.warn("Invalid Request: startDate or endDate is null (startDate={}, endDate={})", start, end);
            return true;
        }

        log.debug("Validating pay period: startDate={}, endDate={}", start, end);

        if (!isEndAfterStart(start, end)) {
            log.warn("Invalid pay period: endDate is not after startDate (startDate={}, endDate={})", start, end);
            addViolation(context, "endDate", "endDate must be after startDate");
            return false;
        }

        if (!isSameMonth(start, end)) {
            log.warn("Invalid pay period: startDate and endDate not within same month (startDate={}, endDate={})", start, end);
            addViolation(context, "startDate", "period must be within a single calendar cycle (same month)");
            return false;
        }

        log.info("PayPeriod validated successfully: {} to {}", start, end);

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
