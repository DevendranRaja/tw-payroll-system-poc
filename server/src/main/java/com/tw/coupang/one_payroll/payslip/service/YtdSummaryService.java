package com.tw.coupang.one_payroll.payslip.service;

import com.tw.coupang.one_payroll.payslip.dto.YtdSummaryForPdfDto;
import com.tw.coupang.one_payroll.payslip.dto.YtdSummaryResponse;

public interface YtdSummaryService {
    YtdSummaryForPdfDto getYtdSummaryWithBreakdown(String employeeId, int year);
    YtdSummaryResponse getYtdSummaryDetails(String employeeId, int year);
}
