package com.tw.coupang.one_payroll.payroll.service.impl;

import com.tw.coupang.one_payroll.payroll.service.GrossToNetStageStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static java.math.BigDecimal.ZERO;

@Component
@Order(2)
public class ValidationStage implements GrossToNetStageStrategy {

    @Override
    public void apply(PayrollContext ctx) {

        if (ctx.getGrossPay().compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("Gross pay must be greater than zero to calculate net pay");
        }

        if (ctx.getTotalDeductions().compareTo(ctx.getGrossPay()) >= 0) {
            throw new IllegalStateException(
                    "Total deductions exceed or equal gross pay, cannot compute net pay"
            );
        }
    }
}
