package com.tw.coupang.one_payroll.payroll.validator;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.payroll.exception.InvalidPayPeriodException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class PayrollCalculationValidator {

    public void validatePayPeriodAgainstPayGroup(LocalDate periodStart, LocalDate periodEnd, PayGroup payGroup) {
        switch (payGroup.getPaymentCycle()) {
            case MONTHLY -> validateMonthlyCycle(periodStart, periodEnd);
            case WEEKLY -> validateWeeklyCycle(periodStart, periodEnd);
            case BIWEEKLY -> validateBiWeeklyCycle(periodStart, periodEnd);
            default -> throw new InvalidPayPeriodException("Unsupported pay cycle: " + payGroup.getPaymentCycle());
        }
    }

    private void validateMonthlyCycle(LocalDate start, LocalDate end) {
        if (start.getMonth() != end.getMonth() || start.getYear() != end.getYear()) {
            throw new InvalidPayPeriodException("Monthly pay cycle must be within the same month");
        }

        if (start.getDayOfMonth() != 1) {
            throw new InvalidPayPeriodException("Monthly pay cycle must start on day 1");
        }

        if (end.getDayOfMonth() != end.lengthOfMonth()) {
            throw new InvalidPayPeriodException("Monthly pay cycle must end on the last day of the month");
        }
    }

    private void validateWeeklyCycle(LocalDate start, LocalDate end) {
        if (ChronoUnit.DAYS.between(start, end) != 6) {
            throw new InvalidPayPeriodException("Weekly pay cycle should be exactly 7 days");
        }
    }

    private void validateBiWeeklyCycle(LocalDate start, LocalDate end) {
        if (ChronoUnit.DAYS.between(start, end) != 13) {
            throw new InvalidPayPeriodException("Bi-weekly pay cycle should be exactly 14 days");
        }
    }
}
