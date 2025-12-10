package com.tw.coupang.one_payroll.payroll.service.calculator.proration;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.payroll.service.calculator.context.ProrationCalculatorContext;
import com.tw.coupang.one_payroll.timesheet.entity.TimesheetSummary;

import java.math.BigDecimal;

public interface ProrationPayCalculator {
    BigDecimal calculate(EmployeeMaster employee, TimesheetSummary timesheet, ProrationCalculatorContext context);
}
