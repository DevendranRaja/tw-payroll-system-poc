package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import com.tw.coupang.one_payroll.payroll.service.impl.IncomeTaxStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.INCOME_TAX;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class IncomeTaxStrategyTest {

    private IncomeTaxStrategy strategy;

    @BeforeEach
    void setup() {
        strategy = new IncomeTaxStrategy();
    }

    @Test
    void shouldCalculateTaxFromGross() {
        //given
        final var payGroup = buildPayGroup();

        // when
        BigDecimal tax = strategy.calculate(BigDecimal.valueOf(30000.00), BigDecimal.ZERO, payGroup);

        // then
        assertEquals(3000.00, tax.doubleValue());
    }

    @Test
    void shouldReturnCode() {
        assertEquals(INCOME_TAX.getValue(), strategy.getCode());
    }

    private PayGroup buildPayGroup() {
        return PayGroup.builder()
                .id(10)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.TEN)
                .benefitRate(BigDecimal.valueOf(5))
                .deductionRate(BigDecimal.ONE)
                .createdAt(LocalDateTime.now())
                .build();
    }
}

