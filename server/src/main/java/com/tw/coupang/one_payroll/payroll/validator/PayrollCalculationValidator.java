package com.tw.coupang.one_payroll.payroll.validator;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.payroll.exception.InvalidPayPeriodException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class PayrollCalculationValidator {

    public void validatePayPeriodAgainstPayGroup(LocalDate periodStart, LocalDate periodEnd, PayGroup payGroup) {
        try {
            switch (payGroup.getPaymentCycle()) {
                case MONTHLY -> validateMonthlyCycle(periodStart, periodEnd);
                case WEEKLY -> validateWeeklyCycle(periodStart, periodEnd);
                case BIWEEKLY -> validateBiWeeklyCycle(periodStart, periodEnd);
                default -> throw new InvalidPayPeriodException("Unsupported pay cycle: " + payGroup.getPaymentCycle());
            }
            log.info("Pay period validated successfully for Employee's PayGroup: {}, cycle: {}, period: {} to {}",
                    payGroup.getGroupName(), payGroup.getPaymentCycle(), periodStart, periodEnd);
        } catch (InvalidPayPeriodException e) {
            log.error("Invalid pay period for Employee's PayGroup: {}, period: {} to {}, reason: {}",
                    payGroup.getGroupName(), periodStart, periodEnd, e.getMessage());
            throw e;
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
