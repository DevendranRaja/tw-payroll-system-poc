package com.tw.coupang.one_payroll.payroll.service.impl;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.payroll.service.DeductionComponentStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.PROFESSIONAL_TAX;
import static java.math.RoundingMode.HALF_UP;

@Component
public class ProfessionalTaxStrategy implements DeductionComponentStrategy {
    private static final BigDecimal FIXED_AMOUNT = BigDecimal.valueOf(200);

    @Override
    public String getCode() {
        return PROFESSIONAL_TAX.getValue();
    }

    @Override
    public BigDecimal calculate(BigDecimal gross, BigDecimal basic, PayGroup payGroup) {
        return FIXED_AMOUNT.setScale(2, HALF_UP);
    }
}
