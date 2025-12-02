package com.tw.coupang.one_payroll.payslip.service;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.employee_master.enums.EmployeeStatus;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.employee_master.repository.EmployeeMasterRepository;
import com.tw.coupang.one_payroll.integration.entity.PayrollRun;
import com.tw.coupang.one_payroll.integration.enums.PayrollStatus;
import com.tw.coupang.one_payroll.payslip.dto.PayslipMetadataDTO;
import com.tw.coupang.one_payroll.payslip.dto.PayslipResponse;
import com.tw.coupang.one_payroll.payslip.entity.Payslip;
import com.tw.coupang.one_payroll.payslip.exception.PayslipNotFoundException;
import com.tw.coupang.one_payroll.payslip.payrollmock.PayrollRunMockRepository;
import com.tw.coupang.one_payroll.payslip.repository.PayslipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayslipServiceImplTest {

    private EmployeeMaster employee;
    private PayrollRun payroll;
    private PayslipMetadataDTO mockMetadata;
    private String employeeId;
    private String payPeriod;
    private LocalDate payPeriodEndOfMonth;
    private Payslip expectedPaySlip;

    @InjectMocks
    private PayslipServiceImpl payslipService;

    @Mock
    private PayrollRunMockRepository payrollRunRepository;

    @Mock
    private EmployeeMasterRepository employeeMasterRepository;

    @Mock
    private PayslipMetadataBuilder metadataBuilder;

    @Mock
    private PayslipRepository payslipRepository;


    @BeforeEach
    void setUp() {
        employeeId = "E001";
        payPeriod = "2025-10";
        payPeriodEndOfMonth = LocalDate.of(2025, 10, 31);

        employee = new EmployeeMaster();
        employee.setEmployeeId(employeeId);
        employee.setFirstName("Jin");
        employee.setLastName("Park");
        employee.setDepartment("Finance");
        employee.setDesignation("Analyst");
        employee.setPayGroupId(1);
        employee.setStatus(EmployeeStatus.ACTIVE);

        payroll = new PayrollRun();
        payroll.setPayrollId(1);
        payroll.setEmployeeId(employeeId);
        payroll.setPayPeriodStart(LocalDate.of(2025, 10, 1));
        payroll.setPayPeriodEnd(LocalDate.of(2025, 10, 31));
        payroll.setGrossPay(new BigDecimal("5000.00"));
        payroll.setTaxDeduction(new BigDecimal("500.00"));
        payroll.setBenefitAddition(new BigDecimal("250.00"));
        payroll.setNetPay(new BigDecimal("4750.00"));
        payroll.setStatus(PayrollStatus.PROCESSED);

        Map<String, BigDecimal> earnings = Map.of(
                "grossPay", new BigDecimal("5000.00"),
                "benefits",  new BigDecimal("250.00")
        );

        Map<String, BigDecimal> deductions = Map.of(
                "tax", new BigDecimal("500.00")
        );


        mockMetadata = PayslipMetadataDTO.builder()
                .employeeId(employeeId)
                .employeeName("Jin Park")
                .department("Finance")
                .designation("Analyst")
                .payPeriod(payPeriodEndOfMonth)
                .payPeriodStart(LocalDate.of(2025, 10, 1))
                .payPeriodEnd(LocalDate.of(2025, 10, 31))
                .grossPay(new BigDecimal("5000.00"))
                .netPay(new BigDecimal("4750.00"))
                .benefitAmount(new BigDecimal("250.00"))
                .earnings(earnings)
                .deductions(deductions)
                .filePath("/E001/OCT2025.pdf")
                .createdAt(LocalDateTime.now())
                .build();

         expectedPaySlip = Payslip.builder()
                .payslipId(1L)
                .employeeId("E001")
                .payrollId(1)
                .payPeriod(LocalDate.of(2025, 10, 31))
                .grossPay(new BigDecimal("5000.00"))
                .netPay(new BigDecimal("4750.00"))
                .benefits(new BigDecimal("250.00"))
                .earnings(earnings)
                .deductions(deductions)
                .filePath("/E001/OCT2025.pdf")
                .build();

    }

    @Test
    void shouldThrowExceptionWhenPayrollIsMissing() {
        when(payrollRunRepository.findPayrollForEmployeeIdAndPayPeriod(employeeId, payPeriod))
                .thenReturn(Optional.empty());

        when(employeeMasterRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));

        assertThrows(IllegalStateException.class,
                () -> payslipService.generatePayslipMetadata(employeeId, payPeriod));
    }

    @Test
    void shouldThrowExceptionWhenEmployeeIdIsMissing() {
        when(employeeMasterRepository.findById(employeeId))
                .thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
                () -> payslipService.generatePayslipMetadata(employeeId, payPeriod));

    }

    @Test
    void shouldThrowExceptionWhenEmployeeIsInactive()
    {
        employee.setStatus(EmployeeStatus.INACTIVE);
        when(employeeMasterRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));

        assertThrows(EmployeeNotFoundException.class,
                () -> payslipService.generatePayslipMetadata(employeeId, payPeriod));
    }

    @Test
    void shouldUpdateExistingPayslipIdempotencyCheck()
    {
        Payslip existingPayslip = new Payslip();
        existingPayslip.setPayslipId(1L);
        existingPayslip.setEmployeeId("E001");

        when(employeeMasterRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));

        when(payrollRunRepository.findPayrollForEmployeeIdAndPayPeriod(employeeId, payPeriod))
                .thenReturn(Optional.of(payroll));

        when(metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth))
                .thenReturn(mockMetadata);

        when(payslipRepository.findByEmployeeIdAndPayPeriod(employeeId,payPeriodEndOfMonth))
                .thenReturn(Optional.of(existingPayslip));

        when(payslipRepository.save(any(Payslip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PayslipMetadataDTO payslipMetadata = payslipService.generatePayslipMetadata(employeeId, payPeriod);

        assertNotNull(payslipMetadata);

        // Verify that existing payslip is updated
        ArgumentCaptor<Payslip> payslipCaptor = ArgumentCaptor.forClass(Payslip.class);
        verify(payslipRepository).save(payslipCaptor.capture());

        Payslip savedPayslip = payslipCaptor.getValue();
        System.out.println("Saved Payslip: " + savedPayslip);
        assertEquals(1L, savedPayslip.getPayslipId()); // Same ID means update
    }

    @Test
    void shouldGenerateNewPayslipData() {
       when(employeeMasterRepository.findById(employeeId))
               .thenReturn(Optional.of(employee));

       when(payrollRunRepository.findPayrollForEmployeeIdAndPayPeriod(employeeId, payPeriod))
               .thenReturn(Optional.of(payroll));

       when(metadataBuilder.buildPayslipMetadata(employee,payroll,payPeriodEndOfMonth))
               .thenReturn(mockMetadata);

       when(payslipRepository.findByEmployeeIdAndPayPeriod(employeeId, payPeriodEndOfMonth))
               .thenReturn(Optional.empty());

       when(payslipRepository.save(any(Payslip.class)))
               .thenAnswer(invocation -> invocation.getArgument(0));

         PayslipMetadataDTO payslipMetadata = payslipService.generatePayslipMetadata(employeeId, payPeriod);

         assertNotNull(payslipMetadata);
         assertEquals("E001", payslipMetadata.getEmployeeId());
         assertEquals("Jin Park", payslipMetadata.getEmployeeName());
        assertEquals(new BigDecimal("5000.00"), payslipMetadata.getGrossPay());

        verify(employeeMasterRepository).findById(employeeId);
        verify(payrollRunRepository).findPayrollForEmployeeIdAndPayPeriod(employeeId, payPeriod);
        verify(metadataBuilder).buildPayslipMetadata(employee, payroll, payPeriodEndOfMonth);
        verify(payslipRepository).save(any(Payslip.class));
    }

    @Test
    void shouldThrowExceptionWhenPayslipIsMissing()
    {
        when(payslipRepository.findByEmployeeIdAndYearMonth(employeeId,payPeriod))
                .thenReturn(Optional.empty());

        assertThrows(PayslipNotFoundException.class,
                () -> payslipService.getPayslipMetadata(employeeId,payPeriod));
    }

    @Test
    void shouldGetPayslipDataSuccessfully()
    {
        when(payslipRepository.findByEmployeeIdAndYearMonth(employeeId,payPeriod))
                .thenReturn(Optional.of(expectedPaySlip));

        PayslipResponse payslipResponse = payslipService.getPayslipMetadata(employeeId, payPeriod);

        assertNotNull(payslipResponse);
        assertEquals(employeeId, payslipResponse.getEmployeeId());
        assertEquals("2025-10-31", payslipResponse.getPeriod());
        assertEquals(new BigDecimal("5000.00"), payslipResponse.getGrossPay());

        verify(payslipRepository).findByEmployeeIdAndYearMonth(employeeId, payPeriod);
    }
}
