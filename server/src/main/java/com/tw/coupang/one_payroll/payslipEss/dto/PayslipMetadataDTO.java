package com.tw.coupang.one_payroll.payslipEss.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayslipMetadataDTO {
    private String employeeId;
    private String employeeName;
    private Integer payrollId;
    private String department;
    private String designation;
    private LocalDate payPeriod;
    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;
    private BigDecimal grossPay;
    private BigDecimal netPay;
    private BigDecimal taxAmount;
    private BigDecimal benefitAmount;
    private Map<String, BigDecimal> earnings;
    private Map<String, BigDecimal> deductions;
    private BigDecimal totalEarnings;
    private BigDecimal totalDeductions;
    private String filePath;
    private LocalDateTime createdAt;
}

