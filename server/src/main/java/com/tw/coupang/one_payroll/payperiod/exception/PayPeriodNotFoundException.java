package com.tw.coupang.one_payroll.payperiod.exception;

import java.time.LocalDate;

public class PayPeriodNotFoundException extends RuntimeException {

    public PayPeriodNotFoundException(Integer payGroupId, LocalDate startDate, LocalDate endDate) {
        super(String.format("No pay period found for payGroupId=%d, startDate=%s, endDate=%s", payGroupId, startDate, endDate));
    }

    public PayPeriodNotFoundException(Integer payPeriodId) {
        super(String.format("No pay period found for payPeriodId=%d", payPeriodId));
    }
}