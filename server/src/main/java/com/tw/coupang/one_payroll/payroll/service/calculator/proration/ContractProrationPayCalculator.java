package com.tw.coupang.one_payroll.payroll.service.calculator.proration;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.payroll.service.calculator.context.ProrationCalculatorContext;
import com.tw.coupang.one_payroll.timesheet.entity.TimesheetSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.common.utils.MathsUtils.safe;
import static java.math.RoundingMode.HALF_UP;

@Slf4j
@Component
public class ContractProrationPayCalculator implements ProrationPayCalculator {

    @Override
    public BigDecimal calculate(EmployeeMaster employee, TimesheetSummary timesheet, ProrationCalculatorContext context) {
        log.info("Calculating contract prorated pay for employeeId: {}, timesheetId: {}", employee.getEmployeeId(), timesheet.getId());

        BigDecimal basePayPerHour = BigDecimal.valueOf(500); // TODO: Hourly basePay - Mock value, shall be replaced with actual basePay from EmployeeMaster

        BigDecimal hoursWorked = safe(timesheet.getHoursWorked());
        BigDecimal extraHoursWorked = safe(timesheet.getHolidayHoursWorked());

        BigDecimal regularPay = basePayPerHour.multiply(hoursWorked);
        BigDecimal holidayPay = basePayPerHour.multiply(context.holidayRate()).multiply(extraHoursWorked);

        return regularPay.add(holidayPay).setScale(2, HALF_UP);
    }
}
