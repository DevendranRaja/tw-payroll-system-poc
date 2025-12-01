package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.employee_master.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.employee_master.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeInactiveException;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.employee_master.Service.EmployeeMasterService;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import com.tw.coupang.one_payroll.paygroups.exception.PayGroupNotFoundException;
import com.tw.coupang.one_payroll.paygroups.validator.PayGroupValidator;
import com.tw.coupang.one_payroll.payroll.dto.request.PayPeriod;
import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.dto.response.ApiResponse;
import com.tw.coupang.one_payroll.payroll.exception.InvalidPayPeriodException;
import com.tw.coupang.one_payroll.payroll.validator.PayrollCalculationValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayrollCalculationServiceImplTest {

    @InjectMocks
    private PayrollCalculationServiceImpl service;

    @Mock
    private EmployeeMasterService employeeMasterService;

    @Mock
    private PayGroupValidator payGroupValidator;

    @Mock
    private PayrollCalculationValidator payrollCalculationValidator;

    @Test
    void shouldThrowEmployeeNotFoundWhenEmployeeMissing() {
        PayrollCalculationRequest request = buildRequest("EMP123");

        when(employeeMasterService.getEmployeeById(request.getEmployeeId()))
                .thenThrow(new EmployeeNotFoundException("Employee not found!"));

        assertThrows(EmployeeNotFoundException.class, () -> service.calculate(request));

        verify(employeeMasterService).getEmployeeById(request.getEmployeeId());
        verifyNoInteractions(payGroupValidator, payrollCalculationValidator);
    }

    @Test
    void shouldThrowEmployeeInactiveWhenEmployeeIsInactive() {
        PayrollCalculationRequest request = buildRequest("EMP123");
        EmployeeMaster employee = buildEmployeeObjectWithInactiveStatus();

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);

        assertThrows(EmployeeInactiveException.class, () -> service.calculate(request));

        verify(employeeMasterService).getEmployeeById(request.getEmployeeId());
        verifyNoInteractions(payGroupValidator, payrollCalculationValidator);
    }

    @Test
    void shouldThrowPayGroupNotFoundWhenMissing() {
        PayrollCalculationRequest request = buildRequest("EMP456");
        EmployeeMaster employee = buildEmployeeObjectWithActiveStatus();

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(employee.getPayGroupId()))
                .thenThrow(new PayGroupNotFoundException("PayGroup not found!"));

        assertThrows(PayGroupNotFoundException.class, () -> service.calculate(request));

        verify(employeeMasterService).getEmployeeById(request.getEmployeeId());
        verify(payGroupValidator).validatePayGroupExists(2);
        verifyNoInteractions(payrollCalculationValidator);
    }

    @Test
    void shouldThrowInvalidPayPeriodWhenValidatorFails() {
        PayrollCalculationRequest request = buildRequest("EMP456");
        EmployeeMaster employee = buildEmployeeObjectWithActiveStatus();
        PayGroup payGroup = buildPayGroup();

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(2)).thenReturn(payGroup);

        doThrow(new InvalidPayPeriodException("Invalid pay period"))
                .when(payrollCalculationValidator)
                .validatePayPeriodAgainstPayGroup(
                        request.getPayPeriod().getStartDate(),
                        request.getPayPeriod().getEndDate(),
                        payGroup);

        assertThrows(InvalidPayPeriodException.class, () -> service.calculate(request));

        verify(employeeMasterService).getEmployeeById(request.getEmployeeId());
        verify(payGroupValidator).validatePayGroupExists(2);
        verify(payrollCalculationValidator).validatePayPeriodAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup);
    }

    @Test
    void shouldReturnSuccessResponseWhenValid() {
        PayrollCalculationRequest request = buildRequest("EMP456");
        EmployeeMaster employee = buildEmployeeObjectWithActiveStatus();
        PayGroup payGroup = buildPayGroup();

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(2)).thenReturn(payGroup);

        doNothing().when(payrollCalculationValidator).validatePayPeriodAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup);

        ApiResponse response = service.calculate(request);

        assertEquals("PAYROLL_CALCULATION_SUCCESS", response.getCode());
        assertEquals("Payroll calculation completed successfully", response.getMessage());
        assertNull(response.getDetails());
        assertNotNull(response.getTimestamp());

        verify(employeeMasterService).getEmployeeById(request.getEmployeeId());
        verify(payGroupValidator).validatePayGroupExists(2);
        verify(payrollCalculationValidator).validatePayPeriodAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup);
    }

    private PayrollCalculationRequest buildRequest(String employeeId) {
        return PayrollCalculationRequest.builder()
                .employeeId(employeeId)
                .payPeriod(
                        PayPeriod.builder()
                                .startDate(LocalDate.of(2025, 11, 1))
                                .endDate(LocalDate.of(2025, 11, 30))
                                .build()
                )
                .build();
    }

    private EmployeeMaster buildEmployeeObjectWithInactiveStatus() {
        return EmployeeMaster.builder()
                .employeeId("EMP123")
                .firstName("John")
                .lastName("Doe")
                .department("Engineering")
                .designation("Software Engineer")
                .email("johndoes@gmail.com")
                .payGroupId(1)
                .status(EmployeeStatus.INACTIVE)
                .joiningDate(LocalDate.of(2024, 12, 15))
                .createdAt(LocalDateTime.of(2024, 12, 15, 10, 0))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private EmployeeMaster buildEmployeeObjectWithActiveStatus() {
        return EmployeeMaster.builder()
                .employeeId("EMP456")
                .firstName("Mary")
                .lastName("Smith")
                .department("Marketing")
                .designation("Marketing Manager")
                .email("marysmith@gmail.com")
                .payGroupId(2)
                .status(EmployeeStatus.ACTIVE)
                .joiningDate(LocalDate.of(2023, 10, 1))
                .createdAt(LocalDateTime.of(2023, 10, 1, 10, 0))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private PayGroup buildPayGroup() {
        return PayGroup.builder()
                .id(10)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.TEN)
                .benefitRate(BigDecimal.valueOf(5))
                .deductionRate(BigDecimal.ONE)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
