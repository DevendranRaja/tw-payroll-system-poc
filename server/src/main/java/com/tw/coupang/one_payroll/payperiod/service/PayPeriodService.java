package com.tw.coupang.one_payroll.payperiod.service;

import com.tw.coupang.one_payroll.payperiod.dto.request.PayPeriodCreateRequest;
import com.tw.coupang.one_payroll.payperiod.dto.response.PayPeriodResponse;

public interface PayPeriodService {
    PayPeriodResponse create(PayPeriodCreateRequest createPayPeriodRequest);
}
