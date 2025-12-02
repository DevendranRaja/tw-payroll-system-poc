package com.tw.coupang.one_payroll.payslip.dto;

import java.math.BigDecimal;

public record YtdSummaryResponse(
        BigDecimal totalGross,
        BigDecimal totalNet,
        BigDecimal totalDeductions,
        BigDecimal totalBenefit
) {

    public YtdSummaryResponse {
        if (totalGross == null) totalGross = BigDecimal.ZERO;
        if (totalNet == null) totalNet = BigDecimal.ZERO;
        if (totalDeductions == null) totalDeductions = BigDecimal.ZERO;
        if (totalBenefit == null) totalBenefit = BigDecimal.ZERO;
    }

    public static YtdSummaryResponse zero() {
        return new YtdSummaryResponse(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }
}