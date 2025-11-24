package com.tw.coupang.one_payroll.payslipEss.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayslipComponentDto
{
    private String type;
    private BigDecimal amount;
}
