package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.payroll.service.impl.BonusStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.BONUS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class BonusStrategyTest {

    private BonusStrategy strategy;

    @BeforeEach
    void setup() {
        strategy = new BonusStrategy();
    }

    @Test
    void shouldCalculateFivePercentBonus() {
        // when
        BigDecimal bonus = strategy.calculate(BigDecimal.ZERO, BigDecimal.valueOf(20000.00));

        // then
        assertEquals(1000.00, bonus.doubleValue());
    }

    @Test
    void shouldReturnCode() {
        assertEquals(BONUS.getValue(), strategy.getCode());
    }
}

