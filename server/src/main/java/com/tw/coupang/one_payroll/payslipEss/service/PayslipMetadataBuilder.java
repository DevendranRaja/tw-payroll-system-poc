package com.tw.coupang.one_payroll.payslipEss.service;

import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.common.constants.PayrollConstants;
import com.tw.coupang.one_payroll.integration.entity.PayrollRun;
import com.tw.coupang.one_payroll.payslipEss.dto.PayslipMetadataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class PayslipMetadataBuilder {

    public static final String PAYSLIP_FILE_PATH_TEMPLATE = "/%s/%s.pdf";

    public PayslipMetadataDTO buildPayslipMetadata(EmployeeMaster employee, PayrollRun payrollRun, LocalDate payPeriodEndOfMonth) {
        log.info("Building payslip metadata for employee: {}, period: {}",
                employee.getEmployeeId(), payrollRun.getPayPeriodEnd());

        Map<String, BigDecimal> earnings = calculateEarnings(payrollRun);
        BigDecimal totalEarnings = earnings.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> deductions = calculateDeductions(payrollRun);
        BigDecimal totalDeductions = deductions.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Format pay period for file path
        String filePathPayPeriod = payrollRun.getPayPeriodEnd()
                .format(DateTimeFormatter.ofPattern("MMMyyyy")).toUpperCase();

        // Generate file path
        String filePath = String.format(
                PAYSLIP_FILE_PATH_TEMPLATE,
                employee.getEmployeeId(),
                filePathPayPeriod);

        return PayslipMetadataDTO.builder()
                .employeeId(employee.getEmployeeId())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .payrollId(payrollRun.getPayrollId())
                .payPeriod(payPeriodEndOfMonth)
                .payPeriodStart(payrollRun.getPayPeriodStart())
                .payPeriodEnd(payrollRun.getPayPeriodEnd())
                .grossPay(payrollRun.getGrossPay())
                .netPay(payrollRun.getNetPay())
                .benefitAmount(payrollRun.getBenefitAddition())
                .earnings(earnings)
                .totalEarnings(totalEarnings)
                .totalDeductions(totalDeductions)
                .deductions(deductions)
                .filePath(filePath)
                .createdAt(LocalDateTime.now())
                .build();

    }

    private Map<String, BigDecimal> calculateEarnings(PayrollRun payrollRun) {
        Map<String, BigDecimal> earnings = new HashMap<>();

        //Adding Gross Pay to Earnings
        if(payrollRun.getGrossPay() != null)
            earnings.put(PayrollConstants.GROSS_PAY, payrollRun.getGrossPay());

        //Adding Benefits to Earnings
        if (payrollRun.getBenefitAddition() != null && payrollRun.getBenefitAddition().compareTo(BigDecimal.ZERO) > 0)
            earnings.put(PayrollConstants.BENEFITS, payrollRun.getBenefitAddition());

        log.info("Earnings: {}", earnings);
        return earnings;

    }

    private Map<String, BigDecimal> calculateDeductions(PayrollRun payrollRun) {
        Map<String, BigDecimal> deductions = new HashMap<>();

        //Adding Tax to Deductions
        if(payrollRun.getTaxDeduction() != null && payrollRun.getTaxDeduction().compareTo(BigDecimal.ZERO) > 0)
            deductions.put(PayrollConstants.TAX, payrollRun.getTaxDeduction());

        log.info("Deductions: {}", deductions);
        return deductions;
    }
}
