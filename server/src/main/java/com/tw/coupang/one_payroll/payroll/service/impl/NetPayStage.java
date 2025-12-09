package com.tw.coupang.one_payroll.payroll.service.impl;

import com.tw.coupang.one_payroll.payroll.service.GrossToNetStageStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

@Component
@Order(4)
public class NetPayStage implements GrossToNetStageStrategy {

    @Override
    public void apply(PayrollContext ctx) {
        BigDecimal net =
                ctx.getGrossPay()
                        .subtract(ctx.getTotalDeductions())
                        .add(ctx.getBenefits())
                        .setScale(2, HALF_UP);

        ctx.setNetPay(net);
    }
}
