package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.payroll.service.impl.DeductionStrategyRegistry;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeductionStrategyRegistryTest {

    @Test
    void shouldRegisterAllStrategies() {

        DeductionComponentStrategy tax = mock(DeductionComponentStrategy.class);
        when(tax.getCode()).thenReturn("Income Tax");

        DeductionComponentStrategy pf = mock(DeductionComponentStrategy.class);
        when(pf.getCode()).thenReturn("Provident Fund");

        DeductionStrategyRegistry registry =
                new DeductionStrategyRegistry(List.of(tax, pf));

        Collection<DeductionComponentStrategy> deductionComponentStrategies = registry.getAll();
        assertTrue(deductionComponentStrategies.contains(tax));
        assertTrue(deductionComponentStrategies.contains(pf));
    }

}

