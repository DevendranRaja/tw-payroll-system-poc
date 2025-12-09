package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.payroll.service.impl.EarningsStrategyRegistry;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.BASIC_SALARY;
import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.BONUS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EarningsStrategyRegistryTest {

    @Test
    void shouldRegisterStrategiesByCode() {

        EarningComponentStrategy basic = mock(EarningComponentStrategy.class);
        when(basic.getCode()).thenReturn(BASIC_SALARY.getValue());

        EarningComponentStrategy bonus = mock(EarningComponentStrategy.class);
        when(bonus.getCode()).thenReturn(BONUS.getValue());

        EarningsStrategyRegistry registry =
                new EarningsStrategyRegistry(List.of(basic, bonus));

        Collection<EarningComponentStrategy> earningComponentStrategies = registry.getAll();
        assertTrue(earningComponentStrategies.contains(basic));
        assertTrue(earningComponentStrategies.contains(bonus));
    }

    @Test
    void shouldReturnAllRegisteredStrategies() {

        EarningComponentStrategy s1 = mock(EarningComponentStrategy.class);
        when(s1.getCode()).thenReturn("A");

        EarningComponentStrategy s2 = mock(EarningComponentStrategy.class);
        when(s2.getCode()).thenReturn("B");

        EarningsStrategyRegistry registry =
                new EarningsStrategyRegistry(List.of(s1, s2));

        assertEquals(2, registry.getAll().size());
    }
}

