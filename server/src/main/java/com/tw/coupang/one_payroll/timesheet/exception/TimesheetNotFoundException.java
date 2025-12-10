package com.tw.coupang.one_payroll.timesheet.exception;

public class TimesheetNotFoundException extends RuntimeException {

    public TimesheetNotFoundException(String employeeId, Integer payPeriodId) {
        super(String.format(
                "No timesheet found for employeeId=%s, payPeriodId=%d",
                employeeId, payPeriodId));
    }
}
