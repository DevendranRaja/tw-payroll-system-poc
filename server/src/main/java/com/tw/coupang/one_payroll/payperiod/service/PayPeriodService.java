package com.tw.coupang.one_payroll.payperiod.service;

import com.tw.coupang.one_payroll.payperiod.dto.request.PayPeriodCreateRequest;
import com.tw.coupang.one_payroll.payperiod.dto.response.PayPeriodResponse;

import java.time.LocalDate;

public interface PayPeriodService {
    PayPeriodResponse create(PayPeriodCreateRequest createPayPeriodRequest);

    void checkOverlap(Integer payGroupId, LocalDate start, LocalDate end);
}
