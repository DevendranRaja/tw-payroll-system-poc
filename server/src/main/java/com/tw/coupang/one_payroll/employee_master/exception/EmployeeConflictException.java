package com.tw.coupang.one_payroll.employee_master.exception;

public class EmployeeConflictException extends RuntimeException {
    public EmployeeConflictException(String message) {
        super(message);
    }

    public EmployeeConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}

