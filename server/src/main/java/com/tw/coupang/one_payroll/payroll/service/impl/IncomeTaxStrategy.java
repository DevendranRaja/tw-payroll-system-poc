package com.tw.coupang.one_payroll.payroll.service.impl;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.payroll.service.DeductionComponentStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.common.utils.MathsUtils.percentOf;
import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.INCOME_TAX;

@Component
public class IncomeTaxStrategy implements DeductionComponentStrategy {

    @Override
    public String getCode() {
        return INCOME_TAX.getValue();
    }

    @Override
    public BigDecimal calculate(BigDecimal gross, BigDecimal basic, PayGroup payGroup) {
        return percentOf(payGroup.getBaseTaxRate(), gross);
    }
}
