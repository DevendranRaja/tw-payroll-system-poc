package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.payroll.service.impl.HRAStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.HRA;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class HRAStrategyTest {

    private HRAStrategy strategy;

    @BeforeEach
    void setup() {
        strategy = new HRAStrategy();
    }

    @Test
    void shouldReturn50PercentOfBasic() {
        // when
        BigDecimal result = strategy.calculate(BigDecimal.ZERO, BigDecimal.valueOf(20000.00));

        // then
        assertEquals(10000.00, result.doubleValue());
    }

    @Test
    void shouldReturnCode() {
        assertEquals(HRA.getValue(), strategy.getCode());
    }
}
