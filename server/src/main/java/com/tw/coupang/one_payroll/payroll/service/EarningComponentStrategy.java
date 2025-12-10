package com.tw.coupang.one_payroll.payroll.service;

import java.math.BigDecimal;

public interface EarningComponentStrategy {
    String getCode();
    BigDecimal calculate(BigDecimal monthlySalary, BigDecimal basicSalary);
}
