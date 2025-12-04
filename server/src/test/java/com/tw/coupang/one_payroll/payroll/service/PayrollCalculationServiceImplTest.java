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
import com.tw.coupang.one_payroll.payperiod.dto.request.PayPeriod;
import com.tw.coupang.one_payroll.payperiod.exception.InvalidPayPeriodException;
import com.tw.coupang.one_payroll.payperiod.validator.PayPeriodCycleValidator;
import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.entity.*;
import com.tw.coupang.one_payroll.payroll.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.tw.coupang.one_payroll.payroll.enums.PayrollStatus.FAILED;
import static com.tw.coupang.one_payroll.payroll.enums.PayrollStatus.PROCESSED;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Map.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollCalculationServiceImplTest {

    private static final String INCOME_TAX = "Income Tax";
    private static final String PROVIDENT_FUND = "Provident Fund";
    private static final String PROFESSIONAL_TAX = "Professional Tax";
    private static final String BASIC_SALARY = "Basic Salary";
    private static final String HRA = "HRA";
    private static final String BONUS = "Bonus";

    @InjectMocks
    private PayrollCalculationServiceImpl service;

    @Mock
    private EmployeeMasterService employeeMasterService;

    @Mock
    private PayrollRunRepository payrollRunRepository;

    @Mock
    private EarningTypeRepository earningTypeRepository;

    @Mock
    private PayrollEarningsRepository payrollEarningsRepository;

    @Mock
    private DeductionTypeRepository deductionTypeRepository;

    @Mock
    private PayrollDeductionsRepository payrollDeductionsRepository;

    @Mock
    private PayGroupValidator payGroupValidator;

    @Mock
    private PayPeriodCycleValidator payPeriodCycleValidator;

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

        EmployeeMaster employee = buildEmployeeObjectWithActiveStatus();

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
        EmployeeMaster employee = buildEmployeeObjectWithActiveStatus();
        PayGroup payGroup = buildPayGroup();

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(2)).thenReturn(payGroup);

        doThrow(new InvalidPayPeriodException("Invalid pay period"))
                .when(payPeriodCycleValidator)
                .validatePayPeriodAgainstPayGroup(
                        request.getPayPeriod().getStartDate(),
                        request.getPayPeriod().getEndDate(),
                        payGroup);

        assertThrows(InvalidPayPeriodException.class, () -> service.calculate(request));

        verify(employeeMasterService).getEmployeeById(request.getEmployeeId());
        verify(payGroupValidator).validatePayGroupExists(2);
        verify(payPeriodCycleValidator).validatePayPeriodAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup);
    }

    @Test
    void shouldReturnSuccessResponseWhenValid() {
        //given
        PayrollCalculationRequest request = buildRequest("EMP456");
        EmployeeMaster employee = buildEmployeeObjectWithBasePay(valueOf(1500.00));
        PayGroup payGroup = buildPayGroup();

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(2)).thenReturn(payGroup);
        when(earningTypeRepository.findAll()).thenReturn(mockEarningTypes());
        when(deductionTypeRepository.findAll()).thenReturn(mockDeductionTypes());

        when(payrollRunRepository.save(any(PayrollRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doNothing().when(payPeriodCycleValidator).validatePayPeriodAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup);

        //when
        final var actual = service.calculate(request);

        //then
        assertEquals(request.getEmployeeId(), actual.employeeId());
        assertEquals(payGroup.getId(), actual.payGroupId());
        verify(employeeMasterService).getEmployeeById(request.getEmployeeId());
        verify(payGroupValidator).validatePayGroupExists(2);
        verify(payPeriodCycleValidator).validatePayPeriodAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup);
        final ArgumentCaptor<PayrollRun> captor = ArgumentCaptor.forClass(PayrollRun.class);
        verify(payrollRunRepository).save(captor.capture());
        assertEquals(employee.getEmployeeId(), captor.getValue().getEmployeeId());
        assertEquals(PROCESSED, captor.getValue().getStatus());
        assertEquals(request.getPayPeriod().getStartDate(), captor.getValue().getPayPeriodStart());
        assertEquals(request.getPayPeriod().getEndDate(), captor.getValue().getPayPeriodEnd());
        assertEquals(27900.00, captor.getValue().getGrossPay().doubleValue());
        assertEquals(23866.00, captor.getValue().getNetPay().doubleValue());
        verify(payrollEarningsRepository).saveAll(anyList());
        verify(payrollDeductionsRepository).saveAll(anyList());
    }

    @Test
    void shouldPersistCorrectEarningAmounts() {
        //given
        PayrollCalculationRequest request = buildRequest("EMP1");
        EmployeeMaster employee = buildEmployeeObjectWithBasePay(valueOf(1500.00));
        PayGroup payGroup = buildPayGroup();

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(2)).thenReturn(payGroup);
        when(earningTypeRepository.findAll()).thenReturn(mockEarningTypes());
        when(deductionTypeRepository.findAll()).thenReturn(mockDeductionTypes());

        when(payrollRunRepository.save(any(PayrollRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //when
        service.calculate(buildRequest("EMP1"));

        //then
        final ArgumentCaptor<List<PayrollEarnings>> captor = ArgumentCaptor.forClass(List.class);
        verify(payrollEarningsRepository).saveAll(captor.capture());

        List<PayrollEarnings> list = captor.getValue();

        assertEquals(3, list.size());

        assertEquals(BASIC_SALARY, list.get(0).getEarningType().getName());
        assertEquals(18000.00, list.get(0).getAmount().doubleValue());
        assertEquals(HRA, list.get(1).getEarningType().getName());
        assertEquals(9000.00, list.get(1).getAmount().doubleValue());
        assertEquals(BONUS, list.get(2).getEarningType().getName());
        assertEquals(900.00, list.get(2).getAmount().doubleValue());
    }

    @Test
    void shouldPersistCorrectDeductionAmounts() {
        //given
        PayrollCalculationRequest request = buildRequest("EMP1");
        EmployeeMaster employee = buildEmployeeObjectWithBasePay(valueOf(1500.00));
        PayGroup payGroup = buildPayGroup();

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(2)).thenReturn(payGroup);
        when(earningTypeRepository.findAll()).thenReturn(mockEarningTypes());
        when(deductionTypeRepository.findAll()).thenReturn(mockDeductionTypes());

        when(payrollRunRepository.save(any(PayrollRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //when
        service.calculate(buildRequest("EMP1"));

        //then
        final ArgumentCaptor<List<PayrollDeductions>> captor = ArgumentCaptor.forClass(List.class);
        verify(payrollDeductionsRepository).saveAll(captor.capture());

        List<PayrollDeductions> list = captor.getValue();

        assertEquals(3, list.size());
        assertEquals(INCOME_TAX, list.get(0).getDeductionType().getName());
        assertEquals(2790.00, list.get(0).getAmount().doubleValue());
        assertEquals(PROVIDENT_FUND, list.get(1).getDeductionType().getName());
        assertEquals(2160.00, list.get(1).getAmount().doubleValue());
        assertEquals(PROFESSIONAL_TAX, list.get(2).getDeductionType().getName());
        assertEquals(200.00, list.get(2).getAmount().doubleValue());
    }

    @Test
    void shouldFailWhenPayrollRunSaveFails() {
        //given
        PayrollCalculationRequest request = buildRequest("EMP1");
        EmployeeMaster employee = buildEmployeeObjectWithBasePay(valueOf(150));
        PayGroup payGroup = buildPayGroup();

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(2)).thenReturn(payGroup);
        when(payrollRunRepository.save(any())).thenThrow(new RuntimeException("DB Down"));

        //when + then
        assertThrows(RuntimeException.class, () -> service.calculate(request));
    }

    @Test
    void testZeroGrossPayReturnsZeroNet() {
        // given
        PayGroup payGroup = PayGroup.builder()
                .baseTaxRate(valueOf(10))
                .benefitRate(valueOf(5))
                .deductionRate(valueOf(2))
                .build();

        Map<String, BigDecimal> deductions = of(
                "Income Tax", ZERO,
                "Provident Fund", ZERO,
                "Professional Tax", ZERO
        );
        PayrollCalculationRequest request = buildRequest("EMP1");

        // when
        final var exception = assertThrows(IllegalArgumentException.class, () ->
                service.payrollGrossToNetPayCalculation(ZERO, deductions, payGroup, request));

        // then
        assertEquals("Gross pay must be greater than zero to calculate net pay", exception.getMessage());
    }

    @Test
    void testNullRatesTreatedAsZero() {
        // given
        PayGroup payGroup = PayGroup.builder()
                .baseTaxRate(null)
                .benefitRate(null)
                .deductionRate(null)
                .build();

        Map<String, BigDecimal> deductions = of(
                "Income Tax", BigDecimal.ZERO,
                "Provident Fund", BigDecimal.ZERO,
                "Professional Tax", BigDecimal.ZERO
        );

        PayrollCalculationRequest request = buildRequest("EMP1");

        // when
        PayrollRun payrollRun = service.payrollGrossToNetPayCalculation(valueOf(20000), deductions, payGroup, request);

        // then
        assertEquals(20000.00, payrollRun.getNetPay().doubleValue());
    }


    @Test
    void testTotalDeductionsExceedsGrossPay() {
        // given
        PayGroup payGroup = PayGroup.builder()
                .baseTaxRate(valueOf(100)) // unused directly
                .benefitRate(BigDecimal.ZERO)
                .deductionRate(valueOf(100))  // 100% deduction of gross
                .build();

        Map<String, BigDecimal> deductions = of(
                "Income Tax", BigDecimal.ZERO,
                "Provident Fund", BigDecimal.ZERO,
                "Professional Tax", BigDecimal.ZERO
        );

        PayrollCalculationRequest request = buildRequest("EMP1");

        // when
        final var exception = assertThrows(IllegalStateException.class, () -> service.payrollGrossToNetPayCalculation(
                valueOf(30000), deductions, payGroup, request));

        // then
        assertEquals("Total deductions exceed or equal gross pay, cannot compute net pay", exception.getMessage());
    }

    @Test
    void testBenefitGreaterThanTaxDoesNotExceedGross() {
        // given
        PayGroup payGroup = PayGroup.builder()
                .baseTaxRate(valueOf(5))
                .benefitRate(valueOf(10))   // +10%
                .deductionRate(BigDecimal.ZERO)
                .build();

        Map<String, BigDecimal> deductions = of(
                "Income Tax", BigDecimal.ZERO,
                "Provident Fund", BigDecimal.ZERO,
                "Professional Tax", BigDecimal.ZERO
        );

        PayrollCalculationRequest request = buildRequest("EMP1");

        // when
        PayrollRun payrollRun = service.payrollGrossToNetPayCalculation(
                valueOf(10000), deductions, payGroup, request);

        // then
        assertEquals(11000.00, payrollRun.getNetPay().doubleValue());
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

    private List<EarningType> mockEarningTypes() {
        return List.of(
                new EarningType(1, "Basic Salary", ""),
                new EarningType(2, "HRA", ""),
                new EarningType(3, "Bonus", "")
        );
    }

    private List<DeductionType> mockDeductionTypes() {
        return List.of(
                new DeductionType(1, "Income Tax", ""),
                new DeductionType(2, "Provident Fund", ""),
                new DeductionType(3, "Professional Tax", "")
        );
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

    private EmployeeMaster buildEmployeeObjectWithBasePay(final BigDecimal basePay) {
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
                .baseSalary(basePay)
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
