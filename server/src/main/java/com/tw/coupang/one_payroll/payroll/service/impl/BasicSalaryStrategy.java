package com.tw.coupang.one_payroll.payroll.service.impl;

import com.tw.coupang.one_payroll.payroll.service.EarningComponentStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.common.utils.MathsUtils.percentOf;
import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.BASIC_SALARY;

@Component
public class BasicSalaryStrategy implements EarningComponentStrategy {

    @Override
    public String getCode() {
        return BASIC_SALARY.getValue();
    }

    @Override
    public BigDecimal calculate(BigDecimal monthlySalary, BigDecimal basic) {
        return percentOf(monthlySalary, BigDecimal.valueOf(40));
    }
}
