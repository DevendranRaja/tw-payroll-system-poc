package com.tw.coupang.one_payroll.payroll.service.impl;

import com.tw.coupang.one_payroll.payroll.service.EarningComponentStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.common.utils.MathsUtils.percentOf;
import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.BONUS;

@Component
public class BonusStrategy implements EarningComponentStrategy {

    @Override
    public String getCode() {
        return BONUS.getValue();
    }

    @Override
    public BigDecimal calculate(BigDecimal monthlySalary, BigDecimal basicSalary) {
        return percentOf(basicSalary, BigDecimal.valueOf(5));
    }
}
