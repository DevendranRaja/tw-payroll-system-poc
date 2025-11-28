package com.tw.coupang.one_payroll.payslipEss.dto;

import java.util.Map;

public record YtdSummaryForPdfDto(
        String employeeId,
        String employeeName,
        String department,
        String designation,
        int year,
        Map<String, MonthlyPayslipSummaryDto> monthlyBreakdown,
        YtdSummaryResponse ytdTotals)
    {
    public YtdSummaryForPdfDto {
        if (monthlyBreakdown == null) {
            monthlyBreakdown = Map.of();
        }
    }
}

