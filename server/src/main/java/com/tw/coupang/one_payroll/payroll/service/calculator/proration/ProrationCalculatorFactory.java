package com.tw.coupang.one_payroll.payroll.service.calculator.proration;

import com.tw.coupang.one_payroll.employee_master.enums.PayType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProrationCalculatorFactory {

    private final Map<PayType, ProrationPayCalculator> prorationPayCalculators;

    public ProrationCalculatorFactory(SalariedProrationPayCalculator salariedProrationPayCalculator, ContractProrationPayCalculator contractProrationPayCalculator) {
        this.prorationPayCalculators = Map.of(
                PayType.SALARIED, salariedProrationPayCalculator,
                PayType.HOURLY, contractProrationPayCalculator
        );
    }

    public ProrationPayCalculator getCalculator(PayType payType) {
        return prorationPayCalculators.get(payType);
    }
}
