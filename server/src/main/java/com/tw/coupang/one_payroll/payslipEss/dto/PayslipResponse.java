package com.tw.coupang.one_payroll.payslipEss.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayslipResponse {
    private String employeeId;
    private String period;
    private Map<String, BigDecimal> earnings;
    private Map<String, BigDecimal> deductions;
    private BigDecimal grossPay;
    private BigDecimal netPay;
    private LocalDateTime createdAt;
}
