package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.employee_master.enums.EmployeeStatus;
import com.tw.coupang.one_payroll.employee_master.enums.PayType;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeInactiveException;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.employee_master.service.EmployeeMasterService;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import com.tw.coupang.one_payroll.paygroups.exception.PayGroupNotFoundException;
import com.tw.coupang.one_payroll.paygroups.validator.PayGroupValidator;
import com.tw.coupang.one_payroll.payperiod.dto.request.PayPeriod;
import com.tw.coupang.one_payroll.payperiod.exception.PayPeriodNotFoundException;
import com.tw.coupang.one_payroll.payperiod.service.PayPeriodService;
import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.entity.PayrollRun;
import com.tw.coupang.one_payroll.payroll.exception.InvalidPayrollStateException;
import com.tw.coupang.one_payroll.payroll.exception.PayrollRunAlreadyExistsException;
import com.tw.coupang.one_payroll.payroll.repository.PayrollRunRepository;
import com.tw.coupang.one_payroll.payperiod.exception.InvalidPayPeriodException;
import com.tw.coupang.one_payroll.payperiod.validator.PayPeriodCycleValidator;
import com.tw.coupang.one_payroll.payroll.service.calculator.proration.ProrationCalculatorFactory;
import com.tw.coupang.one_payroll.payroll.service.calculator.proration.ProrationPayCalculator;
import com.tw.coupang.one_payroll.payroll.validator.PayrollValidator;
import com.tw.coupang.one_payroll.timesheet.entity.TimesheetSummary;
import com.tw.coupang.one_payroll.timesheet.exception.TimesheetNotFoundException;
import com.tw.coupang.one_payroll.timesheet.service.TimesheetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
    private PayrollRunRepository payrollRunRepository;

    @Mock
    private PayGroupValidator payGroupValidator;

    @Mock
    private PayPeriodCycleValidator payPeriodCycleValidator;

    @Mock
    private PayrollValidator payrollValidator;

    @Mock
    private TimesheetService timesheetService;

    @Mock
    private PayPeriodService payPeriodService;

    @Mock
    private ProrationCalculatorFactory prorationCalculatorFactory;

    @Mock
    private ProrationPayCalculator prorationPayCalculator;

    private static final BigDecimal HOLIDAY_RATE = BigDecimal.valueOf(1.5);

    @Test
    void shouldThrowEmployeeNotFoundWhenEmployeeMissing() {
        PayrollCalculationRequest request = buildRequest("EMP123");

        when(employeeMasterService.getEmployeeById(request.getEmployeeId()))
                .thenThrow(new EmployeeNotFoundException("Employee not found!"));

        assertThrows(EmployeeNotFoundException.class, () -> service.calculate(request));

        verify(employeeMasterService).getEmployeeById(request.getEmployeeId());
        verifyNoInteractions(payGroupValidator, payPeriodCycleValidator);
    }

    @Test
    void shouldThrowEmployeeInactiveWhenEmployeeIsInactive() {
        PayrollCalculationRequest request = buildRequest("EMP123");

        EmployeeMaster employee = buildEmployeeObjectWithInactiveStatus();

        when(employeeMasterService.getEmployeeById(request.getEmployeeId()))
                .thenReturn(employee);

        assertThrows(EmployeeInactiveException.class, () -> service.calculate(request));

        verify(employeeMasterService).getEmployeeById(request.getEmployeeId());
        verifyNoInteractions(payGroupValidator, payPeriodCycleValidator);
    }

    @Test
    void shouldThrowPayGroupNotFoundWhenMissing() {
        PayrollCalculationRequest request = buildRequest("EMP456");

        EmployeeMaster employee = buildEmployeeObjectWithActiveStatus(PayType.HOURLY);

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(employee.getPayGroupId()))
                .thenThrow(new PayGroupNotFoundException("PayGroup not found!"));

        assertThrows(PayGroupNotFoundException.class, () -> service.calculate(request));

        verify(employeeMasterService).getEmployeeById(request.getEmployeeId());
        verify(payGroupValidator).validatePayGroupExists(2);
        verifyNoInteractions(payPeriodCycleValidator);
    }

    @Test
    void shouldThrowInvalidPayPeriodWhenValidatorFails() {
        PayrollCalculationRequest request = buildRequest("EMP456");
        EmployeeMaster employee = buildEmployeeObjectWithActiveStatus(PayType.SALARIED);
        PayGroup payGroup = buildPayGroup(HOLIDAY_RATE);

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(2)).thenReturn(payGroup);

        doThrow(new InvalidPayPeriodException("Invalid pay period"))
                .when(payPeriodCycleValidator)
                .validatePayPeriodsAgainstPayGroup(
                        request.getPayPeriod().getStartDate(),
                        request.getPayPeriod().getEndDate(),
                        payGroup);

        assertThrows(InvalidPayPeriodException.class, () -> service.calculate(request));

        verify(employeeMasterService).getEmployeeById(request.getEmployeeId());
        verify(payGroupValidator).validatePayGroupExists(2);
        verify(payPeriodCycleValidator).validatePayPeriodsAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup);
    }

    @ParameterizedTest
    @CsvSource({"HOURLY", "SALARIED"})
    void shouldReturnSuccessResponseWhenValid(String payTypeString) {
        PayrollCalculationRequest request = buildRequest("EMP456");
        EmployeeMaster employee = buildEmployeeObjectWithActiveStatus(PayType.valueOf(payTypeString));
        PayGroup payGroup = buildPayGroup(HOLIDAY_RATE);
        final Integer payPeriodId = 1;
        final BigDecimal proratedPay = BigDecimal.valueOf(2000);

        TimesheetSummary timesheet = TimesheetSummary.builder()
                .employeeId(request.getEmployeeId())
                .payPeriodId(payPeriodId)
                .noOfDaysWorked(20)
                .hoursWorked(BigDecimal.valueOf(160))
                .build();

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(2)).thenReturn(payGroup);

        doNothing().when(payPeriodCycleValidator).validatePayPeriodsAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup);

        when(payPeriodService.getPayPeriodId(employee.getPayGroupId(),
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate()))
                .thenReturn(payPeriodId);

        when(timesheetService.getTimesheet(request.getEmployeeId(), payPeriodId)).thenReturn(timesheet);

        when(prorationCalculatorFactory.getCalculator(employee.getPayType())).thenReturn(prorationPayCalculator);
        when(prorationPayCalculator.calculate(any(), any(), any())).thenReturn(proratedPay);

        final var actual = service.calculate(request);

        assertEquals(request.getEmployeeId(), actual.employeeId());
        assertEquals(payGroup.getId(), actual.payGroupId());
        verify(employeeMasterService).getEmployeeById(request.getEmployeeId());
        verify(payGroupValidator).validatePayGroupExists(2);
        verify(payPeriodCycleValidator).validatePayPeriodsAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup);
        verify(timesheetService).getTimesheet(request.getEmployeeId(), payPeriodId);
        verify(prorationCalculatorFactory).getCalculator(employee.getPayType());
        verify(prorationPayCalculator).calculate(any(), any(), any());

        final ArgumentCaptor<PayrollRun> captor = ArgumentCaptor.forClass(PayrollRun.class);
        verify(payrollRunRepository).save(captor.capture());
        assertEquals(employee.getEmployeeId(), captor.getValue().getEmployeeId());
        assertEquals(PROCESSED, captor.getValue().getStatus());
        assertEquals(request.getPayPeriod().getStartDate(), captor.getValue().getPayPeriodStart());
        assertEquals(request.getPayPeriod().getEndDate(), captor.getValue().getPayPeriodEnd());
        assertEquals(proratedPay, captor.getValue().getGrossPay());
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

    @Test
    void shouldThrowPayPeriodNotFoundExceptionWhenPayPeriodMissing() {
        PayrollCalculationRequest request = buildRequest("EMP789");
        EmployeeMaster employee = buildEmployeeObjectWithActiveStatus(PayType.HOURLY);
        PayGroup payGroup = buildPayGroup(null);

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(employee.getPayGroupId())).thenReturn(payGroup);
        doNothing().when(payPeriodCycleValidator).validatePayPeriodsAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup
        );

        when(payPeriodService.getPayPeriodId(employee.getPayGroupId(), request.getPayPeriod().getStartDate(), request.getPayPeriod().getEndDate()))
                .thenThrow(new PayPeriodNotFoundException(employee.getPayGroupId(), request.getPayPeriod().getStartDate(), request.getPayPeriod().getEndDate()));

        assertThrows(PayPeriodNotFoundException.class, () -> service.calculate(request));
    }

    @Test
    void shouldThrowTimesheetNotFoundExceptionWhenTimesheetMissing() {
        PayrollCalculationRequest request = buildRequest("EMP456");
        EmployeeMaster employee = buildEmployeeObjectWithActiveStatus(PayType.HOURLY);
        PayGroup payGroup = buildPayGroup(HOLIDAY_RATE);
        com.tw.coupang.one_payroll.payperiod.entity.PayPeriod payPeriod = com.tw.coupang.one_payroll.payperiod.entity.PayPeriod.builder()
                .id(1)
                .payGroupId(2)
                .periodStartDate(request.getPayPeriod().getStartDate())
                .periodEndDate(request.getPayPeriod().getEndDate())
                .build();

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(employee.getPayGroupId())).thenReturn(payGroup);
        doNothing().when(payPeriodCycleValidator).validatePayPeriodsAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup
        );

        when(payPeriodService.getPayPeriodId(employee.getPayGroupId(), request.getPayPeriod().getStartDate(), request.getPayPeriod().getEndDate())).thenReturn(payPeriod.getId());

        when(timesheetService.getTimesheet(employee.getEmployeeId(), payPeriod.getId()))
                .thenThrow(new TimesheetNotFoundException(employee.getEmployeeId(), payPeriod.getId()));

        assertThrows(TimesheetNotFoundException.class, () -> service.calculate(request));
    }

    @Test
    void shouldThrowInvalidPayrollStateWhenJoiningAfterPeriodEnd() {
        PayrollCalculationRequest request = buildRequest("EMP999");
        EmployeeMaster employee = buildEmployeeObjectWithActiveStatus(PayType.SALARIED);
        employee.setJoiningDate(LocalDate.of(2026, 1, 15));
        PayGroup payGroup = buildPayGroup(HOLIDAY_RATE);

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(employee.getPayGroupId())).thenReturn(payGroup);

        doThrow(new InvalidPayrollStateException("Joining date after pay period"))
                .when(payrollValidator)
                .validateEmployeeJoiningDateAgainstPayPeriods(
                        employee,
                        request.getPayPeriod().getStartDate(),
                        request.getPayPeriod().getEndDate()
                );

        assertThrows(InvalidPayrollStateException.class, () -> service.calculate(request));
    }

    @Test
    void shouldThrowPayrollRunAlreadyExistsExceptionWhenDuplicatePayroll() {
        PayrollCalculationRequest request = buildRequest("EMP456");
        EmployeeMaster employee = buildEmployeeObjectWithActiveStatus(PayType.HOURLY);
        PayGroup payGroup = buildPayGroup(HOLIDAY_RATE);

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(employee.getPayGroupId())).thenReturn(payGroup);
        doNothing().when(payPeriodCycleValidator).validatePayPeriodsAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup
        );

        when(payrollRunRepository.existsPayrollRunByEmployeeAndPeriod(
                employee.getEmployeeId(),
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate()))
                .thenReturn(true);

        assertThrows(PayrollRunAlreadyExistsException.class,
                () -> service.calculate(request));
    }

    @Test
    void shouldReturnDefaultHolidayRateWhenPayGroupHolidayRateIsNull() {
        PayrollCalculationRequest request = buildRequest("EMP456");
        EmployeeMaster employee = buildEmployeeObjectWithActiveStatus(PayType.SALARIED);
        employee.setPayType(PayType.SALARIED);

        PayGroup payGroup = buildPayGroup(null);
        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(employee.getPayGroupId())).thenReturn(payGroup);

        doNothing().when(payPeriodCycleValidator).validatePayPeriodsAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup
        );

        when(payrollRunRepository.existsPayrollRunByEmployeeAndPeriod(any(), any(), any()))
                .thenReturn(false);

        when(payPeriodService.getPayPeriodId(employee.getPayGroupId(),
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate()))
                .thenReturn(1);

        TimesheetSummary timesheet = TimesheetSummary.builder()
                .employeeId(employee.getEmployeeId())
                .payPeriodId(1)
                .hoursWorked(BigDecimal.TEN)
                .noOfDaysWorked(20)
                .build();

        when(timesheetService.getTimesheet(employee.getEmployeeId(), 1)).thenReturn(timesheet);
        when(prorationCalculatorFactory.getCalculator(employee.getPayType())).thenReturn(prorationPayCalculator);
        when(prorationPayCalculator.calculate(any(), any(), any())).thenReturn(BigDecimal.valueOf(1000));

        final var result = service.calculate(request);

        assertNotNull(result);
        assertEquals(employee.getEmployeeId(), result.employeeId());
    }

    @Test
    void shouldCallProrationCalculatorCorrectlyForHourlyEmployee() {
        PayrollCalculationRequest request = buildRequest("EMP456");
        EmployeeMaster employee = buildEmployeeObjectWithActiveStatus(PayType.HOURLY);
        PayGroup payGroup = buildPayGroup(HOLIDAY_RATE);

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(employee.getPayGroupId())).thenReturn(payGroup);
        doNothing().when(payPeriodCycleValidator).validatePayPeriodsAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup
        );

        when(payrollRunRepository.existsPayrollRunByEmployeeAndPeriod(any(), any(), any())).thenReturn(false);
        when(payPeriodService.getPayPeriodId(anyInt(), any(), any())).thenReturn(1);

        TimesheetSummary timesheet = TimesheetSummary.builder().employeeId(employee.getEmployeeId())
                .payPeriodId(1)
                .hoursWorked(BigDecimal.TEN)
                .noOfDaysWorked(20)
                .build();

        when(timesheetService.getTimesheet(employee.getEmployeeId(), 1)).thenReturn(timesheet);
        when(prorationCalculatorFactory.getCalculator(employee.getPayType())).thenReturn(prorationPayCalculator);
        when(prorationPayCalculator.calculate(any(), any(), any())).thenReturn(BigDecimal.valueOf(1500));

        service.calculate(request);

        verify(prorationPayCalculator).calculate(any(), any(), any());
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
                .payType(PayType.SALARIED)
                .createdAt(LocalDateTime.of(2024, 12, 15, 10, 0))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private EmployeeMaster buildEmployeeObjectWithActiveStatus(PayType payType) {
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
                .payType(payType)
                .createdAt(LocalDateTime.of(2023, 10, 1, 10, 0))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private PayGroup buildPayGroup(BigDecimal holidayRate) {
        return PayGroup.builder()
                .id(10)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.TEN)
                .benefitRate(BigDecimal.valueOf(5))
                .deductionRate(BigDecimal.ONE)
                .holidayRate(holidayRate)
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
