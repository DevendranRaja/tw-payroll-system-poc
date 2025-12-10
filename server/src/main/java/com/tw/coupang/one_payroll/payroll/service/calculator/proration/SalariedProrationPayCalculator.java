package com.tw.coupang.one_payroll.payroll.service.calculator.proration;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.payroll.service.calculator.context.ProrationCalculatorContext;
import com.tw.coupang.one_payroll.timesheet.entity.TimesheetSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.common.utils.MathsUtils.safeInt;
import static java.math.RoundingMode.HALF_UP;

@Slf4j
@Component
public class SalariedProrationPayCalculator implements ProrationPayCalculator {

    @Override
    public BigDecimal calculate(EmployeeMaster employee, TimesheetSummary timesheet, ProrationCalculatorContext context) {
        log.info("Calculating salaried prorated pay for employeeId: {}, timesheetId: {}", employee.getEmployeeId(), timesheet.getId());

        int daysWorked = safeInt(timesheet.getNoOfDaysWorked());
        int holidayDays = safeInt(timesheet.getHolidayDays());

        BigDecimal basePayPerDay = BigDecimal.valueOf(5000); // TODO: Mock value, shall be replaced with actual salary from EmployeeMaster

        int effectiveDays = daysWorked + holidayDays;

        return basePayPerDay.multiply(BigDecimal.valueOf(effectiveDays))
                .divide(BigDecimal.valueOf(context.totalWorkingDays()), 2, HALF_UP);
    }
}
