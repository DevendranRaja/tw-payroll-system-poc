package com.tw.coupang.one_payroll.payperiod.validator;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import com.tw.coupang.one_payroll.payperiod.exception.InvalidPayPeriodException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PayPeriodCycleValidatorTest {

    @InjectMocks
    private PayPeriodCycleValidator validator;

    @ParameterizedTest
    @MethodSource("payPeriodValidationProvider")
    void validatePayPeriods(LocalDate start, LocalDate end, PayGroup payGroup, boolean shouldPass) {

        if (shouldPass) {
            assertDoesNotThrow(
                    () -> validator.validatePayPeriodAgainstPayGroup(start, end, payGroup)
            );
        } else {
            assertThrows(InvalidPayPeriodException.class,
                    () -> validator.validatePayPeriodAgainstPayGroup(start, end, payGroup)
            );
        }
    }

    private static Stream<Arguments> payPeriodValidationProvider() {
        PayGroup monthly = createPayGroupWithMonthlyCycle();
        PayGroup weekly = createPayGroupWithWeeklyCycle();
        PayGroup biweekly = createPayGroupWithBiWeeklyCycle();

        return Stream.of(
                Arguments.of(LocalDate.of(2025, 3, 10), LocalDate.of(2025, 3, 9), monthly, false),
                Arguments.of(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), monthly, true),
                Arguments.of(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 4, 1), monthly, false),
                Arguments.of(LocalDate.of(2025, 3, 2), LocalDate.of(2025, 3, 31), monthly, false),
                Arguments.of(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 30), monthly, false),

                Arguments.of(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 7), weekly, true),
                Arguments.of(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 6), weekly, false),

                Arguments.of(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 14), biweekly, true),
                Arguments.of(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 13), biweekly, false)
        );
    }

    private static PayGroup createPayGroupWithMonthlyCycle() {
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

    private static PayGroup createPayGroupWithWeeklyCycle() {
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

    private static PayGroup createPayGroupWithBiWeeklyCycle() {
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
