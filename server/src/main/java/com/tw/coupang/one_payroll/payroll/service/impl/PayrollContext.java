package com.tw.coupang.one_payroll.payroll.service.impl;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ZERO;

@Data
@AllArgsConstructor
public class PayrollContext {

    private BigDecimal grossPay;
    private BigDecimal totalDeductions = ZERO;
    private BigDecimal benefits = ZERO;
    private BigDecimal netPay = ZERO;

    private Map<String, BigDecimal> deductionMap;
    private PayGroup payGroup;
}
