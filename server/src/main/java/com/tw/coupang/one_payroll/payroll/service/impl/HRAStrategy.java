package com.tw.coupang.one_payroll.payroll.service.impl;

import com.tw.coupang.one_payroll.payroll.service.EarningComponentStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.common.utils.MathsUtils.percentOf;
import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.HRA;

@Component
public class HRAStrategy implements EarningComponentStrategy {

    @Override
    public String getCode() {
        return HRA.getValue();
    }

    @Override
    public BigDecimal calculate(BigDecimal monthlySalary, BigDecimal basicSalary) {
        return percentOf(basicSalary, BigDecimal.valueOf(50));
    }
}
