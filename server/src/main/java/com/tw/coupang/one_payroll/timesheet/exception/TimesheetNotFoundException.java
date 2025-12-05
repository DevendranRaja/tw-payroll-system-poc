package com.tw.coupang.one_payroll.timesheet.exception;

public class TimesheetNotFoundException extends RuntimeException {
    public TimesheetNotFoundException(String message) {
        super(message);
    }
}
