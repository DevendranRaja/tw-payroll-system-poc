package com.tw.coupang.one_payroll.timesheet.helper;


import com.tw.coupang.one_payroll.employee_master.exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.employee_master.repository.EmployeeMasterRepository;
import com.tw.coupang.one_payroll.payperiod.exception.PayPeriodNotFoundException;
import com.tw.coupang.one_payroll.payperiod.repository.PayPeriodRepository;
import com.tw.coupang.one_payroll.timesheet.dto.TimesheetRequest;
import com.tw.coupang.one_payroll.timesheet.exception.InvalidTimesheetException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TimesheetValidator {

    private final EmployeeMasterRepository employeeRepository;
    private final PayPeriodRepository payPeriodRepository;

    public void validateRequest(TimesheetRequest request) {
        //Validate Employee Exists and is Active
        var employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + request.getEmployeeId()));

        if (!"ACTIVE".equalsIgnoreCase(String.valueOf(employee.getStatus()))) {
            throw new InvalidTimesheetException("Employee is not ACTIVE");
        }

        //Validate Pay Period Exists
        if (!payPeriodRepository.existsById(request.getPayPeriodId())) {
            throw new PayPeriodNotFoundException(request.getPayPeriodId());
        }

        //Validate Logical Math
        BigDecimal holiday = request.getHolidayHoursWorked() != null ? request.getHolidayHoursWorked() : BigDecimal.ZERO;

        if (holiday.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTimesheetException("Holiday hours cannot be negative");
        }

        //holidayHours <= hoursWorked
        if (holiday.compareTo(request.getHoursWorked()) > 0) {
            throw new InvalidTimesheetException("Holiday hours cannot be greater than total Hours Worked");
        }

        int daysWorked = request.getNoOfDaysWorked() != null ? request.getNoOfDaysWorked() : 0;
        if (daysWorked < 0) {
            throw new InvalidTimesheetException("Worked days cannot be negative");
        }

        int holidayDays = request.getHolidayDays() != null ? request.getHolidayDays() : 0;
        if (holidayDays < 0) {
            throw new InvalidTimesheetException("Holiday days cannot be negative");
        }

        BigDecimal hoursWorked = request.getHoursWorked() != null ? request.getHoursWorked() : BigDecimal.ZERO;
        if (hoursWorked.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTimesheetException("Worked hours cannot be negative");
        }
    }
}
