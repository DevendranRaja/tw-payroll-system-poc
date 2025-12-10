package com.tw.coupang.one_payroll.payroll.exception;

public class InvalidPayrollStateException extends RuntimeException {
    public InvalidPayrollStateException(String message) {
        super(message);
    }
}
