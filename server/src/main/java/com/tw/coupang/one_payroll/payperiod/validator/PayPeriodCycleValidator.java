package com.tw.coupang.one_payroll.payperiod.validator;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.payperiod.exception.InvalidPayPeriodException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class PayPeriodCycleValidator {

    public void validatePayPeriodAgainstPayGroup(LocalDate startDate, LocalDate endDate, PayGroup payGroup) {
        try {
            switch (payGroup.getPaymentCycle()) {
                case MONTHLY -> validateMonthlyCycle(startDate, endDate);
                case WEEKLY -> validateWeeklyCycle(startDate, endDate);
                case BIWEEKLY -> validateBiWeeklyCycle(startDate, endDate);
                default -> throw new InvalidPayPeriodException("Unsupported pay cycle: " + payGroup.getPaymentCycle());
            }
            log.info("Pay period validated successfully for Employee's PayGroup: {}, cycle: {}, period: {} to {}",
                    payGroup.getGroupName(), payGroup.getPaymentCycle(), startDate, endDate);
        } catch (InvalidPayPeriodException e) {
            log.error("Invalid pay period for Employee's PayGroup: {}, period: {} to {}, reason: {}",
                    payGroup.getGroupName(), startDate, endDate, e.getMessage());
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
