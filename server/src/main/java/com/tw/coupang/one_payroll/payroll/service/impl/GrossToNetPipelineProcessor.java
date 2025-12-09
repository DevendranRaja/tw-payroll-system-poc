package com.tw.coupang.one_payroll.payroll.service.impl;

import com.tw.coupang.one_payroll.payroll.service.GrossToNetStageStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class GrossToNetPipelineProcessor {

    private final List<GrossToNetStageStrategy> stages;

    public GrossToNetPipelineProcessor(List<GrossToNetStageStrategy> stages) {
        this.stages = stages;
    }

    public PayrollContext process(PayrollContext ctx) {
        Objects.requireNonNull(ctx, "PayrollContext must not be null");
        stages.forEach(stage -> stage.apply(ctx));
        return ctx;
    }
}
