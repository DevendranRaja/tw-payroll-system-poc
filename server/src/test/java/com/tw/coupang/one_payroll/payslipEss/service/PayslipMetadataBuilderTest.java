package com.tw.coupang.one_payroll.payslipEss.service;

import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.payslipEss.dto.PayslipItemDto;
import com.tw.coupang.one_payroll.payslipEss.dto.PayslipMetadataDTO;
import com.tw.coupang.one_payroll.payslipEss.payrollmock.PayrollRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

        assertNotNull(payslipMetadata.getEarnings());
        assertEquals(2, payslipMetadata.getEarnings().size());

        PayslipItemDto grossPayItem = payslipMetadata.getEarnings().get(0);
        assertEquals("Gross Pay", grossPayItem.getDescription());
        assertEquals(new BigDecimal("5000.00"), grossPayItem.getAmount());

        PayslipItemDto benefitsItem = payslipMetadata.getEarnings().get(1);
        assertEquals("Benefits", benefitsItem.getDescription());
        assertEquals(new BigDecimal("250.00"), benefitsItem.getAmount());

    }

    @Test
    void shouldNotAddZeroOrNullBenefits()
    {
        payroll.setBenefitAddition(BigDecimal.ZERO);
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        // Should only have gross pay in earnings
        assertNotNull(payslipMetadata.getEarnings());
        assertEquals(1, payslipMetadata.getEarnings().size());
        assertEquals("Gross Pay", payslipMetadata.getEarnings().get(0).getDescription());

        payroll.setBenefitAddition(null);
        payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        // Should only have gross pay in earnings
        assertNotNull(payslipMetadata.getEarnings());
        assertEquals(1, payslipMetadata.getEarnings().size());
        assertEquals("Gross Pay", payslipMetadata.getEarnings().get(0).getDescription());
    }

    @Test
    void shouldCalculateDeductionsCorrectly()
    {
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        assertNotNull(payslipMetadata.getDeductions());
        assertEquals(1, payslipMetadata.getDeductions().size());

        assertEquals("Tax", payslipMetadata.getDeductions().get(0).getDescription());
        assertEquals(new BigDecimal("500.00"), payslipMetadata.getDeductions().get(0).getAmount());
    }

    @Test
    void shouldNotDeductZeroOrNullTax()
    {
        payroll.setTaxDeduction(BigDecimal.ZERO);
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        // Should only have gross pay in earnings
        assertNotNull(payslipMetadata.getDeductions());
        assertEquals(0, payslipMetadata.getDeductions().size());

        payroll.setTaxDeduction(null);
        payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        // Should only have gross pay in earnings
        assertNotNull(payslipMetadata.getDeductions());
        assertEquals(0, payslipMetadata.getDeductions().size());
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
