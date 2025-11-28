package com.tw.coupang.one_payroll.payslipEss.service;

import com.tw.coupang.one_payroll.payslipEss.dto.YtdSummaryForPdfDto;
import com.tw.coupang.one_payroll.payslipEss.dto.YtdSummaryResponse;

public interface YtdSummaryService {
    YtdSummaryForPdfDto getYtdSummaryWithBreakdown(String employeeId, int year);
    YtdSummaryResponse getYtdSummaryDetails(String employeeId, int year);
}
