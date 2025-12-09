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
import com.tw.coupang.one_payroll.payroll.entity.DeductionType;
import com.tw.coupang.one_payroll.payroll.entity.EarningType;
import com.tw.coupang.one_payroll.payroll.entity.PayrollEarnings;
import com.tw.coupang.one_payroll.payroll.entity.PayrollRun;
import com.tw.coupang.one_payroll.payroll.repository.*;
import com.tw.coupang.one_payroll.payroll.service.impl.*;
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

    @Mock
    private GrossToNetPipelineProcessor grossToNetPipelineProcessor;

    @Mock
    private EarningsStrategyRegistry earningsRegistry;

    @Mock
    private DeductionStrategyRegistry deductionRegistry;

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
        payrollMocks();

        doNothing().when(payPeriodCycleValidator).validatePayPeriodAgainstPayGroup(
                request.getPayPeriod().getStartDate(),
                request.getPayPeriod().getEndDate(),
                payGroup);

        PayrollContext context = new PayrollContext(valueOf(27900), valueOf(5150), valueOf(1400),
                        valueOf(23866), deductionMap(), payGroup);

        when(grossToNetPipelineProcessor.process(any())).thenReturn(context);

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
    void shouldInvokeGrossToNetPipeline() {

        PayrollCalculationRequest request = buildRequest("EMP100");
        EmployeeMaster employee = buildEmployeeObjectWithBasePay(valueOf(1500));
        PayGroup payGroup = buildPayGroup();

        when(employeeMasterService.getEmployeeById(any())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(any())).thenReturn(payGroup);
        when(earningTypeRepository.findAll()).thenReturn(mockEarningTypes());
        when(deductionTypeRepository.findAll()).thenReturn(mockDeductionTypes());
        payrollMocks();

        PayrollContext ctx =
                new PayrollContext(valueOf(27900), ZERO, ZERO, valueOf(26000), deductionMap(), payGroup);

        when(grossToNetPipelineProcessor.process(any())).thenReturn(ctx);

        service.calculate(request);

        verify(grossToNetPipelineProcessor, times(1)).process(any());
    }

    @Test
    void shouldFailIfPipelineThrowsException() {

        PayrollCalculationRequest request = buildRequest("EMP_FAIL");
        EmployeeMaster employee = buildEmployeeObjectWithBasePay(valueOf(1500));
        PayGroup payGroup = buildPayGroup();

        when(employeeMasterService.getEmployeeById(any())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(any())).thenReturn(payGroup);
        payrollMocks();

        when(grossToNetPipelineProcessor.process(any()))
                .thenThrow(new IllegalStateException("PF exceeds cap"));

        assertThrows(IllegalStateException.class, () -> service.calculate(request));
        verifyNoInteractions(payrollEarningsRepository);
        verifyNoInteractions(payrollDeductionsRepository);
    }

    @Test
    void shouldFailIfPipelineReturnsNullContext() {

        PayrollCalculationRequest request = buildRequest("EMP_NULL");
        EmployeeMaster employee = buildEmployeeObjectWithBasePay(valueOf(1200));
        PayGroup payGroup = buildPayGroup();

        when(employeeMasterService.getEmployeeById(any())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(any())).thenReturn(payGroup);
        payrollMocks();

        when(grossToNetPipelineProcessor.process(any())).thenReturn(null);

        assertThrows(NullPointerException.class, () -> service.calculate(request));
        verifyNoInteractions(payrollEarningsRepository);
        verifyNoInteractions(payrollDeductionsRepository);
    }

    @Test
    void shouldUsePipelineNetPayValueNotInternalCalculation() {

        PayrollCalculationRequest request = buildRequest("EMP_OVERRIDE");
        EmployeeMaster employee = buildEmployeeObjectWithBasePay(valueOf(2000));
        PayGroup payGroup = buildPayGroup();

        when(employeeMasterService.getEmployeeById(any())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(any())).thenReturn(payGroup);
        when(earningTypeRepository.findAll()).thenReturn(mockEarningTypes());
        when(deductionTypeRepository.findAll()).thenReturn(mockDeductionTypes());
        payrollMocks();

        PayrollContext ctx = new PayrollContext(
                valueOf(50000), valueOf(10000), valueOf(900), valueOf(12345), deductionMap(), payGroup);

        when(grossToNetPipelineProcessor.process(any())).thenReturn(ctx);

        final var response = service.calculate(request);

        assertEquals(12345.00, response.netPay().doubleValue());
    }

    @Test
    void shouldThrowExceptionWhenBaseSalaryIsNegative() {
        //given
        PayrollCalculationRequest request = buildRequest("EMP_NEGATIVE_SALARY");
        EmployeeMaster employee = buildEmployeeObjectWithBasePay(valueOf(-100));

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);

        //when + then
        final var message = assertThrows(IllegalArgumentException.class, () -> service.calculate(request));
        assertEquals("Base salary must be greater than zero for payroll calculation", message.getMessage());
    }

    @Test
    void shouldNotPersistEarningsWhenNoEarningTypesConfigured() {
        //given
        PayrollCalculationRequest request = buildRequest("EMP1");
        EmployeeMaster employee = buildEmployeeObjectWithBasePay(valueOf(150.00));
        PayGroup payGroup = buildPayGroup();

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(2)).thenReturn(payGroup);
        when(earningTypeRepository.findAll()).thenReturn(emptyList());
        when(deductionTypeRepository.findAll()).thenReturn(mockDeductionTypes());
        payrollMocks();

        PayrollContext ctx =
                new PayrollContext(valueOf(50000), valueOf(10000), ZERO, valueOf(12345), deductionMap(), payGroup);

        when(grossToNetPipelineProcessor.process(any())).thenReturn(ctx);

        //when
        service.calculate(request);

        //then
        verify(payrollEarningsRepository, never()).saveAll(anyList());
        verify(payrollDeductionsRepository).saveAll(anyList());
    }

    @Test
    void shouldNotPersistDeductionsWhenNoDeductionTypesConfigured() {
        //given
        PayrollCalculationRequest request = buildRequest("EMP1");
        EmployeeMaster employee = buildEmployeeObjectWithBasePay(valueOf(150.00));
        PayGroup payGroup = buildPayGroup();

        when(employeeMasterService.getEmployeeById(request.getEmployeeId())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(2)).thenReturn(payGroup);
        when(earningTypeRepository.findAll()).thenReturn(mockEarningTypes());
        when(deductionTypeRepository.findAll()).thenReturn(emptyList());
        payrollMocks();

        PayrollContext ctx =
                new PayrollContext(valueOf(50000), valueOf(10000), ZERO, valueOf(12345), deductionMap(), payGroup);

        when(grossToNetPipelineProcessor.process(any())).thenReturn(ctx);

        //when
        service.calculate(request);

        //then
        verify(payrollDeductionsRepository, never()).saveAll(anyList());
        verify(payrollEarningsRepository).saveAll(anyList());
    }

    @Test
    void shouldIgnoreBonusWhenBonusTypeNotPresent() {
        //given
        PayrollCalculationRequest request = buildRequest("EMP1");
        EmployeeMaster employee = buildEmployeeObjectWithBasePay(valueOf(150.00));
        PayGroup payGroup = buildPayGroup();

        List<EarningType> typesWithoutBonus = List.of(
                new EarningType(1, "Basic Salary", ""),
                new EarningType(2, "HRA", "")
        );

        when(employeeMasterService.getEmployeeById(any())).thenReturn(employee);
        when(payGroupValidator.validatePayGroupExists(any())).thenReturn(payGroup);
        when(earningTypeRepository.findAll()).thenReturn(typesWithoutBonus);
        when(deductionTypeRepository.findAll()).thenReturn(mockDeductionTypes());
        payrollMocks();

        PayrollContext ctx = new PayrollContext(
                valueOf(27900.00), valueOf(5150), valueOf(900), valueOf(23866.00), deductionMap(), payGroup);

        when(grossToNetPipelineProcessor.process(any())).thenReturn(ctx);

        //when
        service.calculate(request);

        //then
        ArgumentCaptor<List<PayrollEarnings>> captor = ArgumentCaptor.forClass(List.class);
        verify(payrollEarningsRepository).saveAll(captor.capture());

        assertEquals(2, captor.getValue().size());
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
        payrollMocks();
        PayrollContext ctx =
                new PayrollContext(valueOf(50000), valueOf(10000), ZERO, valueOf(12345), deductionMap(), payGroup);

        when(grossToNetPipelineProcessor.process(any())).thenReturn(ctx);

        //when + then
        assertThrows(RuntimeException.class, () -> service.calculate(request));
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

    private void payrollMocks() {
        when(earningsRegistry.getAll()).thenReturn(List.of(
                new BasicSalaryStrategy(),
                new HRAStrategy(),
                new BonusStrategy()
        ));

        when(deductionRegistry.getAll()).thenReturn(List.of(
                new IncomeTaxStrategy(),
                new ProvidentFundStrategy(),
                new ProfessionalTaxStrategy()
        ));
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

    private Map<String, BigDecimal> deductionMap() {
        return Map.of(
                "Income Tax", valueOf(2500),
                "Provident Fund", valueOf(1800),
                "Professional Tax", valueOf(200)
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
