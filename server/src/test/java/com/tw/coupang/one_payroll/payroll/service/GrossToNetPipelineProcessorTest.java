package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.payroll.service.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.util.Map.of;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GrossToNetPipelineProcessorTest {

    private GrossToNetPipelineProcessor processor;

    private PayrollContext context;

    @BeforeEach
    void setUp() {
        DeductionStage deductionStage = new DeductionStage();
        ValidationStage validationStage = new ValidationStage();
        BenefitStage benefitStage = new BenefitStage();
        NetPayStage netPayStage = new NetPayStage();

        processor = new GrossToNetPipelineProcessor(List.of(deductionStage, validationStage, benefitStage, netPayStage));
    }

    @Test
    void testZeroGrossPayReturnsZeroNet() {
        // given
        PayGroup payGroup = PayGroup.builder()
                .baseTaxRate(valueOf(10))
                .benefitRate(valueOf(5))
                .deductionRate(valueOf(2))
                .build();
        Map<String, BigDecimal> deductions = of(
                "Income Tax", ZERO,
                "Provident Fund", ZERO,
                "Professional Tax", ZERO
        );
        context = new PayrollContext(ZERO, ZERO, ZERO, ZERO, deductions, payGroup);

        // when
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> processor.process(context));

        // then
        assertEquals("Gross pay must be greater than zero to calculate net pay", ex.getMessage());
    }

    @Test
    void testNullRatesTreatedAsZero() {
        // given
        PayGroup payGroup = PayGroup.builder()
                .baseTaxRate(null)
                .benefitRate(null)
                .deductionRate(null)
                .build();

        Map<String, BigDecimal> deductions = of(
                "Income Tax", ZERO,
                "Provident Fund", ZERO,
                "Professional Tax", ZERO
        );
        context = new PayrollContext(valueOf(20000), ZERO, ZERO, ZERO, deductions, payGroup);

        // when
        PayrollContext result = processor.process(context);

        // then
        assertEquals(20000.00, result.getNetPay().doubleValue());
    }

    @Test
    void testTotalDeductionsExceedsGrossPay() {
        // given
        PayGroup payGroup = PayGroup.builder()
                .baseTaxRate(valueOf(100))
                .benefitRate(ZERO)
                .deductionRate(valueOf(100))
                .build();

        Map<String, BigDecimal> deductions = of(
                "Income Tax", ZERO,
                "Provident Fund", ZERO,
                "Professional Tax", ZERO
        );
        context = new PayrollContext(valueOf(20000), ZERO, ZERO, ZERO, deductions, payGroup);

        // when
        Exception ex = assertThrows(IllegalStateException.class,
                () -> processor.process(context));

        // then
        assertEquals("Total deductions exceed or equal gross pay, cannot compute net pay", ex.getMessage());
    }

    @Test
    void testBenefitGreaterThanTaxDoesNotExceedGross() {
        // given
        PayGroup payGroup = PayGroup.builder()
                .baseTaxRate(valueOf(5))
                .benefitRate(valueOf(10))
                .deductionRate(ZERO)
                .build();
        Map<String, BigDecimal> deductions = of(
                "Income Tax", ZERO,
                "Provident Fund", ZERO,
                "Professional Tax", ZERO
        );
        context = new PayrollContext(valueOf(10000), ZERO, ZERO, ZERO, deductions, payGroup);


        // when
        PayrollContext result = processor.process(context);

        // then
        assertEquals(11000.00, result.getNetPay().doubleValue());
    }

    @Test
    void shouldFailWhenContextMissingPayGroup() {
        context = new PayrollContext(valueOf(10000), ZERO, ZERO, ZERO, Map.of(), null);

        Exception ex = assertThrows(NullPointerException.class,
                () -> processor.process(context));

        assertTrue(ex.getMessage().contains("PayGroup"));
    }

    @Test
    void shouldFailFastIfContextIsNull() {

        assertThrows(NullPointerException.class,
                () -> processor.process(null));
    }

    @Test
    void shouldFailIfContextNullExplicitMessage() {
        Exception e = assertThrows(NullPointerException.class,
                () -> processor.process(null));

        assertTrue(e.getMessage().contains("PayrollContext"));
    }

}

