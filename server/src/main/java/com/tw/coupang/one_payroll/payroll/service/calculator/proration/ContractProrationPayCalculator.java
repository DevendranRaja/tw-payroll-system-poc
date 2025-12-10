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

    private static final BigDecimal HOURS_PER_DAY = BigDecimal.valueOf(8);

    @Override
    public BigDecimal calculate(EmployeeMaster employee, TimesheetSummary timesheet, ProrationCalculatorContext context) {
        log.info("Calculating contract prorated pay for employeeId: {}, timesheetId: {}", employee.getEmployeeId(), timesheet.getId());

        BigDecimal basePayPerHour = BigDecimal.valueOf(5000).divide(HOURS_PER_DAY, 2, HALF_UP); // TODO: Mock value, shall be replaced with actual salary from EmployeeMaster

        BigDecimal hoursWorked = safe(timesheet.getHoursWorked());
        BigDecimal extraHoursWorked = safe(timesheet.getHolidayHoursWorked());

        return basePayPerHour.multiply(hoursWorked)
                .add(basePayPerHour.multiply(context.holidayRate()).multiply(extraHoursWorked))
                .setScale(2, HALF_UP);
    }
}
