package com.tw.coupang.one_payroll.payroll.service.impl;

import com.tw.coupang.one_payroll.payroll.service.DeductionComponentStrategy;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DeductionStrategyRegistry {

    private Map<String, DeductionComponentStrategy> strategyMap;

    public DeductionStrategyRegistry(List<DeductionComponentStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(DeductionComponentStrategy::getCode, s -> s));
    }

    public Collection<DeductionComponentStrategy> getAll() {
        return strategyMap.values();
    }
}
