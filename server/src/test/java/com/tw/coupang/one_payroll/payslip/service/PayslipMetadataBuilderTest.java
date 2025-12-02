package com.tw.coupang.one_payroll.payslip.service;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.employee_master.enums.EmployeeStatus;
import com.tw.coupang.one_payroll.integration.entity.PayrollRun;
import com.tw.coupang.one_payroll.integration.enums.PayrollStatus;
import com.tw.coupang.one_payroll.payslip.dto.PayslipMetadataDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PayslipMetadataBuilderTest {

    private PayslipMetadataBuilder metadataBuilder;
    private EmployeeMaster employee;
    private PayrollRun payroll;
    private LocalDate payPeriodEndOfMonth;

    @BeforeEach
    void setUp() {
        metadataBuilder = new PayslipMetadataBuilder();

        employee = new EmployeeMaster();
        employee.setEmployeeId("E001");
        employee.setFirstName("Jin");
        employee.setLastName("Park");
        employee.setDepartment("Finance");
        employee.setDesignation("Analyst");
        employee.setPayGroupId(1);
        employee.setStatus(EmployeeStatus.ACTIVE);

        payroll = new PayrollRun();
        payroll.setPayrollId(1);
        payroll.setEmployeeId("E001");
        payroll.setPayPeriodStart(LocalDate.of(2025, 10, 1));
        payroll.setPayPeriodEnd(LocalDate.of(2025, 10, 31));
        payroll.setGrossPay(new BigDecimal("5000.00"));
        payroll.setTaxDeduction(new BigDecimal("500.00"));
        payroll.setBenefitAddition(new BigDecimal("250.00"));
        payroll.setNetPay(new BigDecimal("4750.00"));
        payroll.setStatus(PayrollStatus.PROCESSED);

        payPeriodEndOfMonth= LocalDate.of(2025,10,31);
    }

    @Test
    void shouldBuildPaySlipSuccessfully() {
        // When
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        // Then
        assertNotNull(payslipMetadata);
        assertEquals("E001", payslipMetadata.getEmployeeId());
        assertEquals("Jin Park", payslipMetadata.getEmployeeName());
        assertEquals("Finance", payslipMetadata.getDepartment());
        assertEquals("Analyst", payslipMetadata.getDesignation());
        assertEquals(LocalDate.of(2025,10,31), payslipMetadata.getPayPeriod());
        assertEquals(LocalDate.of(2025, 10, 1), payslipMetadata.getPayPeriodStart());
        assertEquals(LocalDate.of(2025, 10, 31), payslipMetadata.getPayPeriodEnd());
    }

    @Test
    void shouldTestEarningsMapHasCorrectValue()
    {
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        Map<String, BigDecimal> earnings = payslipMetadata.getEarnings();
        assertNotNull(earnings);
        assertEquals(2, earnings.size());
        assertEquals(new BigDecimal("5000.00"), earnings.get("grossPay"));
        assertEquals(new BigDecimal("250.00"), earnings.get("benefits"));
    }

    @Test
    void shouldNotAddZeroOrNullBenefits()
    {
        payroll.setBenefitAddition(BigDecimal.ZERO);
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        Map<String, BigDecimal> earnings = payslipMetadata.getEarnings();
        // Should only have gross pay in earnings
        assertNotNull(earnings);
        assertEquals(1, earnings.size());
        assertEquals(new BigDecimal("5000.00"), earnings.get("grossPay"));

        payroll.setBenefitAddition(null);
        payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);
        earnings = payslipMetadata.getEarnings();

        // Should only have gross pay in earnings
        assertNotNull(earnings);
        assertEquals(1, earnings.size());
        assertEquals(new BigDecimal("5000.00"), earnings.get("grossPay"));
    }

    @Test
    void shouldCalculateTotalEarningsCorrectly()
    {
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        BigDecimal expectedTotalEarnings = new BigDecimal("5250.00");

        assertNotNull(expectedTotalEarnings);
        assertEquals(expectedTotalEarnings, payslipMetadata.getTotalEarnings());
    }

    @Test
    void shouldTestDeductionsMapHasCorrectValue()
    {
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        Map<String, BigDecimal> deductions = payslipMetadata.getDeductions();

        assertNotNull(deductions);
        assertEquals(1, deductions.size());
        assertEquals(new BigDecimal("500.00"), deductions.get("tax"));
    }

    @Test
    void shouldNotDeductZeroOrNullTax()
    {
        payroll.setTaxDeduction(BigDecimal.ZERO);
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);
        Map<String, BigDecimal> deductions = payslipMetadata.getDeductions();

        assertNotNull(deductions);
        assertEquals(0, deductions.size());

        payroll.setTaxDeduction(null);
        payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);
        deductions = payslipMetadata.getDeductions();

        assertNotNull(deductions);
        assertEquals(0, deductions.size());
    }

    @Test
    void shouldCalculateTotalDeductionsCorrectly()
    {
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        BigDecimal expectedTotalDeductions = new BigDecimal("500.00");

        assertNotNull(expectedTotalDeductions);
        assertEquals(expectedTotalDeductions, payslipMetadata.getTotalDeductions());
    }

    @Test
    void shouldCheckIfFilePathIsGeneratedCorrectly()
    {
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        String expectedFilePath = "/E001/OCT2025.pdf";
        String actualFilePath = payslipMetadata.getFilePath();
        assertEquals(expectedFilePath, actualFilePath);
    }
}
