package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.dto.response.ApiResponse;

public interface PayrollCalculationService {
    ApiResponse calculate(PayrollCalculationRequest request);
}
