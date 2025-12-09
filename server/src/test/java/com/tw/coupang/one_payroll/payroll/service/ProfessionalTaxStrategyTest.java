package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.payroll.service.impl.ProfessionalTaxStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.PROFESSIONAL_TAX;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ProfessionalTaxStrategyTest {

    private ProfessionalTaxStrategy strategy;

    @BeforeEach
    void setup() {
        strategy = new ProfessionalTaxStrategy();
    }

    @Test
    void shouldAlwaysReturnFlatAmount() {
        // given
        final var payGroup = PayGroup.builder().build();

        // when
        BigDecimal tax = strategy.calculate(BigDecimal.ZERO, BigDecimal.valueOf(50000.00), payGroup);

        // then
        assertEquals(200.00, tax.doubleValue());
    }

    @Test
    void shouldReturnCode() {
        assertEquals(PROFESSIONAL_TAX.getValue(), strategy.getCode());
    }
}

