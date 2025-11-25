package com.tw.coupang.one_payroll.payslipEss.service;

import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.payslipEss.dto.PayslipItemDto;
import com.tw.coupang.one_payroll.payslipEss.dto.PayslipMetadataDTO;
import com.tw.coupang.one_payroll.payslipEss.payrollmock.PayrollRun;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PayslipMetadataBuilder {

    public static final String PAYSLIP_FILE_PATH_TEMPLATE = "/payslips/%s_%s.pdf";

    public PayslipMetadataDTO buildPayslipMetadata(EmployeeMaster employee, PayrollRun payrollRun) {
        log.info("Building payslip metadata for employee: {}, period: {}",
                employee.getEmployeeId(), payrollRun.getPayPeriodEnd());
        
        List<PayslipItemDto> earnings = calculateEarnings(payrollRun);
        List<PayslipItemDto> deductions = calculateDeductions(payrollRun);

        // Format pay period for file path
        String filePathPayPeriod = payrollRun.getPayPeriodEnd()
                .format(DateTimeFormatter.ofPattern("MMMyyyy")).toUpperCase();

        // Generate file path
        String filePath = String.format(
                PAYSLIP_FILE_PATH_TEMPLATE,
                employee.getEmployeeId(),
                filePathPayPeriod.replace("-", "")
        );

        return PayslipMetadataDTO.builder()
                .employeeId(employee.getEmployeeId())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .payPeriod(payrollRun.getPayPeriodEnd())
                .payPeriodStart(payrollRun.getPayPeriodStart())
                .payPeriodEnd(payrollRun.getPayPeriodEnd())
                .grossPay(payrollRun.getGrossPay())
                .netPay(payrollRun.getNetPay())
                .taxAmount(payrollRun.getTaxDeduction())
                .benefitAmount(payrollRun.getBenefitAddition())
                .earnings(earnings)
                .deductions(deductions)
                .filePath(filePath)
                .createdAt(LocalDateTime.now())
                .build();

    }

    private List<PayslipItemDto> calculateEarnings(PayrollRun payrollRun) {
        List<PayslipItemDto> earnings = new ArrayList<>();

        //Adding Gross Pay to Earnings
        earnings.add(PayslipItemDto.builder()
                .description("Gross Pay")
                .amount(payrollRun.getGrossPay())
                .build());

        //Adding Benefits to Earnings
        if (payrollRun.getBenefitAddition() != null && payrollRun.getBenefitAddition().compareTo(BigDecimal.ZERO) > 0) {
            earnings.add(PayslipItemDto.builder()
                    .description("Benefits")
                    .amount(payrollRun.getBenefitAddition())
                    .build());
        }

        log.info("Earnings: {}", earnings);
        return earnings;

    }

    private List<PayslipItemDto> calculateDeductions(PayrollRun payrollRun) {
        List<PayslipItemDto> deductions = new ArrayList<>();

        //Adding Tax to Deductions
        if(payrollRun.getTaxDeduction() != null && payrollRun.getTaxDeduction().compareTo(BigDecimal.ZERO) > 0) {

            deductions.add(
                    PayslipItemDto.builder()
                            .description("Tax")
                            .amount(payrollRun.getTaxDeduction())
                            .build()
            );
        }

        log.info("Deductions: {}", deductions);
        return deductions;
    }
}
