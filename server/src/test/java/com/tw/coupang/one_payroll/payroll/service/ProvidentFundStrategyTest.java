package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.payroll.service.impl.ProvidentFundStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.PROVIDENT_FUND;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ProvidentFundStrategyTest {

    private ProvidentFundStrategy strategy;

    @BeforeEach
    void setup() {
        strategy = new ProvidentFundStrategy();
    }

    @Test
    void shouldCalculateProvidentFund() {
        // given
        final var payGroup = PayGroup.builder().build();

        // when
        BigDecimal pf = strategy.calculate(BigDecimal.ZERO, BigDecimal.valueOf(20000.00), payGroup);

        // then
        assertEquals(2400.00, pf.doubleValue());
    }

    @Test
    void shouldReturnCode() {
        assertEquals(PROVIDENT_FUND.getValue(), strategy.getCode());
    }
}

