package com.tw.coupang.one_payroll.payroll.service.impl;

import com.tw.coupang.one_payroll.payroll.service.GrossToNetStageStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.common.utils.MathsUtils.percentOf;

@Component
@Order(3)
public class BenefitStage implements GrossToNetStageStrategy {

    @Override
    public void apply(PayrollContext ctx) {
        BigDecimal benefit = percentOf(ctx.getPayGroup().getBenefitRate(), ctx.getGrossPay());
        ctx.setBenefits(benefit);
    }
}
