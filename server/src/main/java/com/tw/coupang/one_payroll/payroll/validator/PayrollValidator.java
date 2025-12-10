package com.tw.coupang.one_payroll.payroll.validator;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.payroll.exception.InvalidPayrollStateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class PayrollValidator {
    public void validateEmployeeJoiningDateAgainstPayPeriods(EmployeeMaster employee, LocalDate periodStart, LocalDate periodEnd) {
        LocalDate joiningDate = employee.getJoiningDate();
        log.info("Validating joining date for employeeId={} against pay period {} to {}", employee.getEmployeeId(), periodStart, periodEnd);

        if (joiningDate.isAfter(periodEnd)) {
            throw new InvalidPayrollStateException(
                    String.format(
                            "Employee %s joined on %s which is after the pay period %s to %s",
                            employee.getEmployeeId(),
                            joiningDate,
                            periodStart,
                            periodEnd
                    )
            );
        }
    }
}
