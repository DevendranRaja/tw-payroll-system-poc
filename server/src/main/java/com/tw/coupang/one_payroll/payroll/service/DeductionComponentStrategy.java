package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;

import java.math.BigDecimal;

public interface DeductionComponentStrategy {
    String getCode();
    BigDecimal calculate(BigDecimal gross, BigDecimal basic, PayGroup payGroup);
}
