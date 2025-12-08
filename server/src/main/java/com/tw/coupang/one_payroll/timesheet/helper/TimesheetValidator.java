package com.tw.coupang.one_payroll.timesheet.helper;


import com.tw.coupang.one_payroll.employee_master.repository.EmployeeMasterRepository;
import com.tw.coupang.one_payroll.payperiod.repository.PayPeriodRepository;
import com.tw.coupang.one_payroll.timesheet.dto.TimesheetRequest;
import com.tw.coupang.one_payroll.timesheet.exception.InvalidTimesheetException;
import com.tw.coupang.one_payroll.timesheet.exception.TimesheetNotFoundException;
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
                .orElseThrow(() -> new TimesheetNotFoundException("Employee not found: " + request.getEmployeeId()));

        if (!"ACTIVE".equalsIgnoreCase(String.valueOf(employee.getStatus()))) {
            throw new InvalidTimesheetException("Employee is not ACTIVE");
        }

        //Validate Pay Period Exists
        if (!payPeriodRepository.existsById(request.getPayPeriodId())) {
            throw new TimesheetNotFoundException("Pay Period not found: " + request.getPayPeriodId());
        }

        //Validate Logical Math
        BigDecimal holiday = request.getHolidayHours() != null ? request.getHolidayHours() : BigDecimal.ZERO;

        if (holiday.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTimesheetException("Holiday hours cannot be negative");
        }

        //holidayHours <= hoursWorked
        if (holiday.compareTo(request.getHoursWorked()) > 0) {
            throw new InvalidTimesheetException("Holiday hours cannot be greater than total Hours Worked");
        }
    }
}
