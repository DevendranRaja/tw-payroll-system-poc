package com.tw.coupang.one_payroll.EmployeeMaster.Exception;

public class EmployeeConflictException extends RuntimeException {
    public EmployeeConflictException(String message) {
        super(message);
    }

    public EmployeeConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}

