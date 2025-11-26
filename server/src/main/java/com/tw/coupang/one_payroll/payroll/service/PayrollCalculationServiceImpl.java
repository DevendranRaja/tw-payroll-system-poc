package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.EmployeeMaster.Repository.EmployeeMasterRepository;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.repository.PayGroupRepository;
import com.tw.coupang.one_payroll.payroll.dto.response.PayrollRunResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

@Service
@AllArgsConstructor
public class PayrollCalculationServiceImpl {

    private final EmployeeMasterRepository employeeMasterRepository;
    private final PayGroupRepository payGroupRepository;

    public PayrollRunResponse calculatePayroll() {
        return null;
    }

    public BigDecimal payrollGrossToNetPayCalculation(final BigDecimal grossPay,
                                                      final PayGroup payGroup) {
        final var tax = percentOf(payGroup.getBaseTaxRate(), grossPay);
        final var benefits = percentOf(payGroup.getBenefitRate(), grossPay);
        final var otherDeductions = percentOf(payGroup.getDeductionRate(), grossPay);
        return grossPay.subtract(tax).subtract(otherDeductions).add(benefits).setScale(2, HALF_UP);
    }

    private BigDecimal percentOf(final BigDecimal percent, final BigDecimal amount) {
        if (percent == null) return BigDecimal.ZERO;
        return amount.multiply(percent).divide(BigDecimal.valueOf(100), 2, HALF_UP);
    }


}
