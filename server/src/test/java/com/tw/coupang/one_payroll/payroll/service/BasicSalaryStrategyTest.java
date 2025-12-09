package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.payroll.service.impl.BasicSalaryStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.BASIC_SALARY;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class BasicSalaryStrategyTest {

    private BasicSalaryStrategy strategy;

    @BeforeEach
    void setup() {
        strategy = new BasicSalaryStrategy();
    }

    @Test
    void shouldReturnCorrectBasicSalaryPercentage() {
        // when
        BigDecimal amount = strategy.calculate(BigDecimal.valueOf(30000.00), BigDecimal.ZERO);

        // then
        assertEquals(12000.00, amount.doubleValue());
    }

    @Test
    void shouldReturnCorrectCode() {
        assertEquals(BASIC_SALARY.getValue(), strategy.getCode());
    }
}

