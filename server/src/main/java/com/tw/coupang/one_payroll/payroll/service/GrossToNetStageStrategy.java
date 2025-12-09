package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.payroll.service.impl.PayrollContext;

public interface GrossToNetStageStrategy {
    void apply(PayrollContext context);
}
