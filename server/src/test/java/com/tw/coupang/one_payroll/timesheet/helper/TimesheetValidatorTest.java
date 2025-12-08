package com.tw.coupang.one_payroll.timesheet.helper;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.employee_master.enums.EmployeeStatus;
import com.tw.coupang.one_payroll.employee_master.repository.EmployeeMasterRepository;
import com.tw.coupang.one_payroll.payperiod.repository.PayPeriodRepository;
import com.tw.coupang.one_payroll.timesheet.dto.TimesheetRequest;
import com.tw.coupang.one_payroll.timesheet.exception.InvalidTimesheetException;
import com.tw.coupang.one_payroll.timesheet.exception.TimesheetNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimesheetValidatorTest {

    @Mock
    private EmployeeMasterRepository employeeRepository;

    @Mock
    private PayPeriodRepository payPeriodRepository;

    @InjectMocks
    private TimesheetValidator validator;

    private TimesheetRequest request;

    @BeforeEach
    void setUp() {
        request = new TimesheetRequest();
        request.setEmployeeId("EMP001");
        request.setPayPeriodId(101);
        request.setHoursWorked(new BigDecimal("40.0"));
        request.setNoOfDaysWorked(5);
        request.setHolidayHours(new BigDecimal("0.0"));
    }

    @Test
    void validateRequestSuccess() {
        EmployeeMaster mockEmployee = new EmployeeMaster();
        mockEmployee.setEmployeeId("EMP001");
        mockEmployee.setStatus(EmployeeStatus.valueOf("ACTIVE")); // Assuming status is a String or Enum

        when(employeeRepository.findById("EMP001")).thenReturn(Optional.of(mockEmployee));
        when(payPeriodRepository.existsById(101)).thenReturn(true);

        assertDoesNotThrow(() -> validator.validateRequest(request));
    }

    @Test
    void validateRequestEmployeeNotFoundThrowsException() {
        when(employeeRepository.findById("EMP001")).thenReturn(Optional.empty());

        assertThrows(TimesheetNotFoundException.class, () -> validator.validateRequest(request));
    }

    @Test
    void validateRequestEmployeeNotActiveThrowsException() {
        EmployeeMaster mockEmployee = new EmployeeMaster();
        mockEmployee.setEmployeeId("EMP001");
        mockEmployee.setStatus(EmployeeStatus.valueOf("INACTIVE"));

        when(employeeRepository.findById("EMP001")).thenReturn(Optional.of(mockEmployee));

        assertThrows(InvalidTimesheetException.class, () -> validator.validateRequest(request));
    }

    @Test
    void validateRequestPayPeriodNotFoundThrowsException() {
        EmployeeMaster mockEmployee = new EmployeeMaster();
        mockEmployee.setStatus(EmployeeStatus.valueOf("ACTIVE"));

        when(employeeRepository.findById("EMP001")).thenReturn(Optional.of(mockEmployee));
        when(payPeriodRepository.existsById(101)).thenReturn(false);

        assertThrows(TimesheetNotFoundException.class, () -> validator.validateRequest(request));
    }

    @Test
    void validateRequestNegativeHolidayHoursThrowsException() {
        EmployeeMaster mockEmployee = new EmployeeMaster();
        mockEmployee.setStatus(EmployeeStatus.valueOf("ACTIVE"));

        when(employeeRepository.findById("EMP001")).thenReturn(Optional.of(mockEmployee));
        when(payPeriodRepository.existsById(101)).thenReturn(true);

        request.setHolidayHours(new BigDecimal("-1.0"));

        assertThrows(InvalidTimesheetException.class, () -> validator.validateRequest(request));
    }

    @Test
    void validateRequestHolidayHoursExceedsWorkedThrowsException() {
        EmployeeMaster mockEmployee = new EmployeeMaster();
        mockEmployee.setStatus(EmployeeStatus.valueOf("ACTIVE"));

        when(employeeRepository.findById("EMP001")).thenReturn(Optional.of(mockEmployee));
        when(payPeriodRepository.existsById(101)).thenReturn(true);

        request.setHoursWorked(new BigDecimal("8.0"));
        request.setHolidayHours(new BigDecimal("9.0")); // Greater than worked

        assertThrows(InvalidTimesheetException.class, () -> validator.validateRequest(request));
    }
}
