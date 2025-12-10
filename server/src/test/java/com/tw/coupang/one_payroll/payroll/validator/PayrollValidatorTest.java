package com.tw.coupang.one_payroll.payroll.validator;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.payroll.exception.InvalidPayrollStateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class PayrollValidatorTest {

    @InjectMocks
    private PayrollValidator payrollValidator;

    @Test
    void shouldPassValidationWhenJoiningDateBeforeOrDuringPayPeriod() {
        EmployeeMaster employee = new EmployeeMaster();
        employee.setEmployeeId("EMP001");
        employee.setJoiningDate(LocalDate.of(2025, 11, 10));

        LocalDate periodStart = LocalDate.of(2025, 11, 1);
        LocalDate periodEnd = LocalDate.of(2025, 11, 30);

        assertDoesNotThrow(() ->
                payrollValidator.validateEmployeeJoiningDateAgainstPayPeriods(employee, periodStart, periodEnd)
        );
    }

    @Test
    void shouldThrowExceptionWhenJoiningDateAfterPayPeriod() {
        EmployeeMaster employee = new EmployeeMaster();
        employee.setEmployeeId("EMP002");
        employee.setJoiningDate(LocalDate.of(2025, 12, 1));

        LocalDate periodStart = LocalDate.of(2025, 11, 1);
        LocalDate periodEnd = LocalDate.of(2025, 11, 30);

        InvalidPayrollStateException exception = assertThrows(
                InvalidPayrollStateException.class,
                () -> payrollValidator.validateEmployeeJoiningDateAgainstPayPeriods(employee, periodStart, periodEnd)
        );

        assertTrue(exception.getMessage().contains("EMP002"));
        assertTrue(exception.getMessage().contains("2025-12-01"));
        assertTrue(exception.getMessage().contains("2025-11-01 to 2025-11-30"));
    }
}
