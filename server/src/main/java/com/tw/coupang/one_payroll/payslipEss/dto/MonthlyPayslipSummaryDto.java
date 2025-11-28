package com.tw.coupang.one_payroll.payslipEss.dto;

import java.math.BigDecimal;

public record MonthlyPayslipSummaryDto(
        String monthName,
        int monthNumber,
        int year,
        BigDecimal grossPay,
        BigDecimal netPay,
        BigDecimal benefit,
        BigDecimal deductions
) {
    public MonthlyPayslipSummaryDto {
        if (grossPay == null) grossPay = BigDecimal.ZERO;
        if (netPay == null) netPay = BigDecimal.ZERO;
        if (benefit == null) benefit = BigDecimal.ZERO;
        if (deductions == null) deductions = BigDecimal.ZERO;
    }
}
