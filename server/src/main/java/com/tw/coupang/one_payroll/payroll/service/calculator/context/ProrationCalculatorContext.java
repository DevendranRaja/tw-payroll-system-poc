package com.tw.coupang.one_payroll.payroll.service.calculator.context;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProrationCalculatorContext(Integer totalWorkingDaysInPayPeriod, BigDecimal holidayRate) { }
