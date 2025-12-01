package com.tw.coupang.one_payroll.employee_master.Exception;

public class EmployeeConflictException extends RuntimeException {
    public EmployeeConflictException(String message) {
        super(message);
    }

    public EmployeeConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}

