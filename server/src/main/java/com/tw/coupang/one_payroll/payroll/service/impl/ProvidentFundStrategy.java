package com.tw.coupang.one_payroll.payroll.service.impl;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.payroll.service.DeductionComponentStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.PROVIDENT_FUND;
import static java.math.RoundingMode.HALF_UP;

@Component
public class ProvidentFundStrategy implements DeductionComponentStrategy {

    private static final BigDecimal PF_RATE = BigDecimal.valueOf(0.12);

    @Override
    public String getCode() {
        return PROVIDENT_FUND.getValue();
    }

    @Override
    public BigDecimal calculate(BigDecimal gross, BigDecimal basic, PayGroup payGroup) {
        return basic.multiply(PF_RATE).setScale(2, HALF_UP);
    }
}
