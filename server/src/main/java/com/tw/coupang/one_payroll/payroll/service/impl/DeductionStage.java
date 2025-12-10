package com.tw.coupang.one_payroll.payroll.service.impl;

import com.tw.coupang.one_payroll.payroll.service.GrossToNetStageStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.common.utils.MathsUtils.percentOf;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

@Component
@Order(1)
public class DeductionStage implements GrossToNetStageStrategy {

    @Override
    public void apply(PayrollContext ctx) {

        BigDecimal otherDeductions =
                percentOf(ctx.getPayGroup().getDeductionRate(),
                        ctx.getGrossPay());

        BigDecimal total =
                ctx.getDeductionMap().values().stream()
                        .reduce(ZERO, BigDecimal::add)
                        .add(otherDeductions)
                        .setScale(2, HALF_UP);

        ctx.setTotalDeductions(total);
    }
}
