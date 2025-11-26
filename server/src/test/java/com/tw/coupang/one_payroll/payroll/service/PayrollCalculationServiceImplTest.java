package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.EmployeeMaster.Repository.EmployeeMasterRepository;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import com.tw.coupang.one_payroll.paygroups.repository.PayGroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PayrollCalculationServiceImplTest {

    @Mock
    private PayGroupRepository payGroupRepository;

    @Mock
    private EmployeeMasterRepository employeeMasterRepository;

    @InjectMocks
    private PayrollCalculationServiceImpl payrollCalculationService;

    @Test
    void testPayrollGrossToNetPayCalculation() {
        // given
        final var payGroup = PayGroup.builder().id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.TEN)
                .benefitRate(valueOf(5.0))
                .deductionRate(valueOf(2.0))
                .createdAt(LocalDateTime.now())
                .build();
        // when
        final var netPay = payrollCalculationService.payrollGrossToNetPayCalculation(valueOf(5000.00), payGroup);

        // then
        assertNotNull(netPay);
        assertEquals(4650.00, netPay.doubleValue());
    }

    @Test
    void testZeroGrossPayReturnsZeroNet() {
        // given
        final var payGroup = PayGroup.builder().id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.TEN)
                .benefitRate(valueOf(5.0))
                .deductionRate(valueOf(2.0))
                .createdAt(LocalDateTime.now())
                .build();

        // when
        final var netPay = payrollCalculationService.payrollGrossToNetPayCalculation(BigDecimal.ZERO, payGroup);

        //then
        assertEquals(BigDecimal.ZERO.setScale(2), netPay);
    }

    @Test
    void testNullRatesTreatedAsZero() {
        // given
        final var payGroup = PayGroup.builder().id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(null)
                .benefitRate(null)
                .deductionRate(null)
                .createdAt(LocalDateTime.now())
                .build();

        // when
        final var netPay = payrollCalculationService.payrollGrossToNetPayCalculation(valueOf(20000.00), payGroup);

        // then
        assertEquals(20000.00, netPay.doubleValue());
    }

    @Test
    void testHundredPercentTaxZeroNet() {
        // given
        final var payGroup = PayGroup.builder().id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(valueOf(100.0))
                .benefitRate(valueOf(0.0))
                .deductionRate(valueOf(0.0))
                .createdAt(LocalDateTime.now())
                .build();

        // when
        final var netPay = payrollCalculationService.payrollGrossToNetPayCalculation(valueOf(30000.00), payGroup);

        // then
        assertEquals(0.00, netPay.doubleValue());
    }

    @Test
    void testBenefitGreaterThanTaxDoesNotExceedGross() {
        // given
        final var payGroup = PayGroup.builder().id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.WEEKLY)
                .baseTaxRate(valueOf(5.0))
                .benefitRate(valueOf(10.0))
                .deductionRate(ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        // when
        final var netPay = payrollCalculationService.payrollGrossToNetPayCalculation(valueOf(10000.00), payGroup);

        // then
        assertEquals(10500.00, netPay.doubleValue());
    }

    @Test
    void testNegativeRatesAreHandledMathematically() {
        // given
        final var payGroup = PayGroup.builder().id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(valueOf(-5.0))
                .benefitRate(valueOf(5.0))
                .deductionRate(ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        // when
        final var netPay = payrollCalculationService.payrollGrossToNetPayCalculation(valueOf(10000.00), payGroup);

        // then
        assertEquals(11000.00, netPay.doubleValue());
    }
}
