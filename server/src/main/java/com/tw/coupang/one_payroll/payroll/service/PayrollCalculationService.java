package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.dto.response.PayrollRunResponse;

import java.time.LocalDate;
import java.util.List;

public interface PayrollCalculationService {
    PayrollRunResponse calculate(PayrollCalculationRequest request);

    List<PayrollRunResponse> getPayroll(String employeeId, LocalDate periodStart, LocalDate periodEnd);
}
