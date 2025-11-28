package com.tw.coupang.one_payroll.payslipEss.service;

import com.tw.coupang.one_payroll.payslipEss.dto.YtdSummaryForPdfDto;

public interface YtdSummaryService {
    YtdSummaryForPdfDto getYtdSummaryWithBreakdown(String employeeId, int year);
}
