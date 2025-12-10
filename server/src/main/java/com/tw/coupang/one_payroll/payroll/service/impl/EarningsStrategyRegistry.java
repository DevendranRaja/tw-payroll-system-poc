package com.tw.coupang.one_payroll.payroll.service.impl;

import com.tw.coupang.one_payroll.payroll.service.EarningComponentStrategy;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Component
public class EarningsStrategyRegistry {

    private Map<String, EarningComponentStrategy> strategyMap;

    public EarningsStrategyRegistry(List<EarningComponentStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .filter(s -> nonNull(s.getCode()))
                .collect(Collectors.toMap(EarningComponentStrategy::getCode, s -> s));
    }

    public Collection<EarningComponentStrategy> getAll() {
        return strategyMap.values();
    }
}
