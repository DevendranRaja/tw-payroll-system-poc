package com.tw.coupang.one_payroll.payslipEss.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayslipItemDto
{
    private String description;
    private BigDecimal amount;
}
