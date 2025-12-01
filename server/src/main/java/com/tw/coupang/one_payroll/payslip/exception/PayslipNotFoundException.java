package com.tw.coupang.one_payroll.payslip.exception;

public class PayslipNotFoundException extends RuntimeException {
    public PayslipNotFoundException(String message) {
        super(message);
    }
}
