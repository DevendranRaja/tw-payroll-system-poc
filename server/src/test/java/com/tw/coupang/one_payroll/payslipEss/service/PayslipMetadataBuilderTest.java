package com.tw.coupang.one_payroll.payslipEss.service;

import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.payslipEss.dto.PayslipMetadataDTO;
import com.tw.coupang.one_payroll.payslipEss.payrollmock.PayrollRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PayslipMetadataBuilderTest {

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
        payroll.setStatus(PayrollRun.PayrollStatus.PROCESSED);

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
    void shouldCalculateEarningsCorrectly()
    {
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        Map<String, BigDecimal> earnings = payslipMetadata.getEarnings();
        assertNotNull(earnings);
        assertEquals(2, earnings.size());
        assertEquals("5000", earnings.get("grossPay").stripTrailingZeros().toPlainString());
        assertEquals("250", earnings.get("benefits").stripTrailingZeros().toPlainString());
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
        assertEquals("5000", earnings.get("grossPay").stripTrailingZeros().toPlainString());

        payroll.setBenefitAddition(null);
        payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);
        earnings = payslipMetadata.getEarnings();

        // Should only have gross pay in earnings
        assertNotNull(earnings);
        assertEquals(1, earnings.size());
        assertEquals("5000", earnings.get("grossPay").stripTrailingZeros().toPlainString());
    }

    @Test
    void shouldCalculateDeductionsCorrectly()
    {
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        Map<String, BigDecimal> deductions = payslipMetadata.getDeductions();

        assertNotNull(deductions);
        assertEquals(1, deductions.size());
        assertEquals("500", deductions.get("tax").stripTrailingZeros().toPlainString());
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
    void shouldCheckIfFilePathIsGeneratedCorrectly()
    {
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        String expectedFilePath = "/payslips/E001_OCT2025.pdf";
        String actualFilePath = payslipMetadata.getFilePath();
        assertEquals(expectedFilePath, actualFilePath);
    }
}
