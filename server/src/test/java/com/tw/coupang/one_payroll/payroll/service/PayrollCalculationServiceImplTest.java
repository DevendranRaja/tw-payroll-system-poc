package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.employee_master.enums.EmployeeStatus;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeInactiveException;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.employee_master.service.EmployeeMasterService;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import com.tw.coupang.one_payroll.paygroups.exception.PayGroupNotFoundException;
import com.tw.coupang.one_payroll.paygroups.validator.PayGroupValidator;
import com.tw.coupang.one_payroll.payroll.dto.request.PayPeriod;
import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.entity.PayrollRun;
import com.tw.coupang.one_payroll.payroll.exception.InvalidPayPeriodException;
import com.tw.coupang.one_payroll.payroll.repository.PayrollRunRepository;
import com.tw.coupang.one_payroll.payroll.validator.PayrollCalculationValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.tw.coupang.one_payroll.payroll.enums.PayrollStatus.FAILED;
import static com.tw.coupang.one_payroll.payroll.enums.PayrollStatus.PROCESSED;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollCalculationServiceImplTest {

    @InjectMocks
    private PayrollCalculationServiceImpl service;

    @Mock
    private EmployeeMasterService employeeMasterService;

    @Mock
    private PayrollRunRepository payrollRunRepository;

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

        when(employeeMasterService.getEmployeeById(request.getEmployeeId()))
                .thenReturn(employee);

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

        final var actual = service.calculate(request);

        assertEquals(request.getEmployeeId(), actual.employeeId());
        assertEquals(payGroup.getId(), actual.payGroupId());
        verify(employeeMasterService).getEmployeeById(request.getEmployeeId());
        verify(payGroupValidator).validatePayGroupExists(2);
        verify(payrollCalculationValidator).validatePayPeriodAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup);
        final ArgumentCaptor<PayrollRun> captor = ArgumentCaptor.forClass(PayrollRun.class);
        verify(payrollRunRepository).save(captor.capture());
        assertEquals(employee.getEmployeeId(), captor.getValue().getEmployeeId());
        assertEquals(PROCESSED, captor.getValue().getStatus());
        assertEquals(request.getPayPeriod().getStartDate(), captor.getValue().getPayPeriodStart());
        assertEquals(request.getPayPeriod().getEndDate(), captor.getValue().getPayPeriodEnd());
    }

    @Test
    void testPayrollGrossToNetPayCalculation() {
        // given
        final var payGroup = PayGroup.builder().id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.TEN)
                .benefitRate(valueOf(5.0))
                .deductionRate(valueOf(2.0))
                .createdAt(LocalDateTime.now())
                .build();
        final var payrollRun = PayrollRun.builder();

        // when
        final var netPay = service.payrollGrossToNetPayCalculation(valueOf(5000.00), payGroup, payrollRun);

        // then
        assertNotNull(netPay);
        assertEquals(4650.00, netPay.doubleValue());
    }

    @Test
    void testZeroGrossPayReturnsZeroNet() {
        // given
        final var payGroup = PayGroup.builder().id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.TEN)
                .benefitRate(valueOf(5.0))
                .deductionRate(valueOf(2.0))
                .createdAt(LocalDateTime.now())
                .build();
        final var payrollRun = PayrollRun.builder();

        // when
        final var netPay = service.payrollGrossToNetPayCalculation(BigDecimal.ZERO, payGroup, payrollRun);

        //then
        assertEquals(BigDecimal.ZERO.setScale(2), netPay);
    }

    @Test
    void testNullRatesTreatedAsZero() {
        // given
        final var payGroup = PayGroup.builder().id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(null)
                .benefitRate(null)
                .deductionRate(null)
                .createdAt(LocalDateTime.now())
                .build();
        final var payrollRun = PayrollRun.builder();

        // when
        final var netPay = service.payrollGrossToNetPayCalculation(valueOf(20000.00), payGroup, payrollRun);

        // then
        assertEquals(20000.00, netPay.doubleValue());
    }

    @Test
    void testHundredPercentTaxZeroNet() {
        // given
        final var payGroup = PayGroup.builder().id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(valueOf(100.0))
                .benefitRate(valueOf(0.0))
                .deductionRate(valueOf(0.0))
                .createdAt(LocalDateTime.now())
                .build();
        final var payrollRun = PayrollRun.builder();

        // when
        final var netPay = service.payrollGrossToNetPayCalculation(valueOf(30000.00), payGroup, payrollRun);

        // then
        assertEquals(0.00, netPay.doubleValue());
    }

    @Test
    void testBenefitGreaterThanTaxDoesNotExceedGross() {
        // given
        final var payGroup = PayGroup.builder().id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.WEEKLY)
                .baseTaxRate(valueOf(5.0))
                .benefitRate(valueOf(10.0))
                .deductionRate(ZERO)
                .createdAt(LocalDateTime.now())
                .build();
        final var payrollRun = PayrollRun.builder();

        // when
        final var netPay = service.payrollGrossToNetPayCalculation(valueOf(10000.00), payGroup, payrollRun);

        // then
        assertEquals(10500.00, netPay.doubleValue());
    }

    @Test
    void testGetPayrollShouldReturnAllPayrollRun() {
        // given
        String employeeId = "EMP123";
        LocalDate periodStart = LocalDate.of(2025, 1, 1);
        LocalDate periodEnd = LocalDate.of(2025, 1, 31);
        when(payrollRunRepository.findByEmployeeIdOrPayPeriodStartAndPayPeriodEnd(employeeId, periodStart, periodEnd))
                .thenReturn(singletonList(buildPayrollRunWithEmployeeIdAndPeriod(employeeId, periodStart, periodEnd)));

        // when
        final var payrollRunResponses = service.getPayroll(employeeId, periodStart, periodEnd);

        // then
        assertNotNull(payrollRunResponses);
        assertEquals(1, payrollRunResponses.size());
        assertEquals(employeeId, payrollRunResponses.get(0).employeeId());
        assertEquals(periodStart, payrollRunResponses.get(0).payPeriodStart());
        assertEquals(periodEnd, payrollRunResponses.get(0).payPeriodEnd());
        assertEquals(45000.00, payrollRunResponses.get(0).netPay().doubleValue());
    }

    @Test
    void testGetPayrollShouldReturnEmptyPayrollRunIfDataNotFound() {
        // given
        String employeeId = "EMP123";
        LocalDate periodStart = LocalDate.of(2025, 1, 1);
        LocalDate periodEnd = LocalDate.of(2025, 1, 31);
        when(payrollRunRepository.findByEmployeeIdOrPayPeriodStartAndPayPeriodEnd(employeeId, periodStart, periodEnd))
                .thenReturn(emptyList());

        // when
        final var payrollRunResponses = service.getPayroll(employeeId, periodStart, periodEnd);

        // then
        assertNotNull(payrollRunResponses);
        assertTrue(payrollRunResponses.isEmpty());
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

    private PayrollRun buildPayrollRunWithEmployeeIdAndPeriod(final String employeeId,
                                                              final LocalDate periodStart,
                                                              final LocalDate periodEnd) {
        return PayrollRun.builder()
                .employeeId(employeeId)
                .payPeriodStart(periodStart)
                .payPeriodEnd(periodEnd)
                .grossPay(BigDecimal.valueOf(50000))
                .netPay(BigDecimal.valueOf(45000))
                .taxDeduction(BigDecimal.valueOf(5000))
                .benefitAddition(BigDecimal.valueOf(2000))
                .status(FAILED)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
