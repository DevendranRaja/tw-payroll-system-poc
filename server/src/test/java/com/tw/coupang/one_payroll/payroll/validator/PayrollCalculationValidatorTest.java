package com.tw.coupang.one_payroll.payroll.validator;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import com.tw.coupang.one_payroll.payroll.exception.InvalidPayPeriodException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PayrollCalculationValidatorTest {

    @InjectMocks
    private PayrollCalculationValidator validator;

    @Test
    void validatePayPeriodEndBeforeStartThrowsException() {
        LocalDate start = LocalDate.of(2025, 3, 10);
        LocalDate end = LocalDate.of(2025, 3, 9);
        PayGroup pg = createPayGroupWithMonthlyCycle();

        assertThrows(InvalidPayPeriodException.class,
                () -> validator.validatePayPeriodAgainstPayGroup(start, end, pg));
    }

    @Test
    void validateMonthlyValidRangePasses() {
        LocalDate start = LocalDate.of(2025, 3, 1);
        LocalDate end = LocalDate.of(2025, 3, 31);
        PayGroup pg = createPayGroupWithMonthlyCycle();

        assertDoesNotThrow(
                () -> validator.validatePayPeriodAgainstPayGroup(start, end, pg));
    }

    @Test
    void validateMonthlyCrossMonthThrowsException() {
        LocalDate start = LocalDate.of(2025, 3, 1);
        LocalDate end = LocalDate.of(2025, 4, 1);
        PayGroup pg = createPayGroupWithMonthlyCycle();

        assertThrows(InvalidPayPeriodException.class,
                () -> validator.validatePayPeriodAgainstPayGroup(start, end, pg));
    }

    @Test
    void validateMonthlyInvalidStartDayThrowsException() {
        LocalDate start = LocalDate.of(2025, 3, 2);
        LocalDate end = LocalDate.of(2025, 3, 31);
        PayGroup pg = createPayGroupWithMonthlyCycle();

        assertThrows(InvalidPayPeriodException.class,
                () -> validator.validatePayPeriodAgainstPayGroup(start, end, pg));
    }

    @Test
    void validateMonthlyInvalidEndDayThrowsException() {
        LocalDate start = LocalDate.of(2025, 3, 1);
        LocalDate end = LocalDate.of(2025, 3, 30);
        PayGroup pg = createPayGroupWithMonthlyCycle();

        assertThrows(InvalidPayPeriodException.class,
                () -> validator.validatePayPeriodAgainstPayGroup(start, end, pg));
    }

    @Test
    void validateWeeklyExactRangePasses() {
        LocalDate start = LocalDate.of(2025, 3, 1);
        LocalDate end = start.plusDays(6);
        PayGroup pg = createPayGroupWithWeeklyCycle();

        assertDoesNotThrow(() ->
                validator.validatePayPeriodAgainstPayGroup(start, end, pg));
    }

    @Test
    void validateWeeklyWrongLengthThrowsException() {
        LocalDate start = LocalDate.of(2025, 3, 1);
        LocalDate end = start.plusDays(5);
        PayGroup pg = createPayGroupWithWeeklyCycle();

        assertThrows(InvalidPayPeriodException.class,
                () -> validator.validatePayPeriodAgainstPayGroup(start, end, pg));
    }

    @Test
    void validateBiWeeklyExactRangePasses() {
        LocalDate start = LocalDate.of(2025, 3, 1);
        LocalDate end = start.plusDays(13);
        PayGroup pg = createPayGroupWithBiWeeklyCycle();

        assertDoesNotThrow(() ->
                validator.validatePayPeriodAgainstPayGroup(start, end, pg));
    }

    @Test
    void validateBiWeeklyWrongLengthThrowsException() {
        LocalDate start = LocalDate.of(2025, 3, 1);
        LocalDate end = start.plusDays(12);
        PayGroup pg = createPayGroupWithBiWeeklyCycle();

        assertThrows(InvalidPayPeriodException.class,
                () -> validator.validatePayPeriodAgainstPayGroup(start, end, pg));
    }

    private PayGroup createPayGroupWithMonthlyCycle() {
        return PayGroup.builder()
                .id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.TEN)
                .benefitRate(BigDecimal.valueOf(5))
                .deductionRate(BigDecimal.valueOf(2))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private PayGroup createPayGroupWithWeeklyCycle() {
        return PayGroup.builder()
                .id(2)
                .groupName("Marketing")
                .paymentCycle(PaymentCycle.WEEKLY)
                .baseTaxRate(BigDecimal.valueOf(15))
                .benefitRate(BigDecimal.valueOf(5))
                .deductionRate(BigDecimal.valueOf(5))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private PayGroup createPayGroupWithBiWeeklyCycle() {
        return PayGroup.builder()
                .id(3)
                .groupName("Sales")
                .paymentCycle(PaymentCycle.BIWEEKLY)
                .baseTaxRate(BigDecimal.TEN)
                .benefitRate(BigDecimal.valueOf(5))
                .deductionRate(BigDecimal.ONE)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
