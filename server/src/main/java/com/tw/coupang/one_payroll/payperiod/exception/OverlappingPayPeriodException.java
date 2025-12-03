package com.tw.coupang.one_payroll.payperiod.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OverlappingPayPeriodException extends RuntimeException {
    public OverlappingPayPeriodException(String message) {
        super(message);
    }
}
