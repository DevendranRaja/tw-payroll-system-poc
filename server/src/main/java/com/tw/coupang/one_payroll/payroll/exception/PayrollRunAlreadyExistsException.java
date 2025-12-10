package com.tw.coupang.one_payroll.payroll.exception;

import java.time.LocalDate;

public class PayrollRunAlreadyExistsException extends RuntimeException {
    public PayrollRunAlreadyExistsException(String employeeId, LocalDate start, LocalDate end) {
        super("Payroll run already exists for employee " + employeeId + " for period " + start + " to " + end);
    }
}
