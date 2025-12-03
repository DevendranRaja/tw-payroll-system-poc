package com.tw.coupang.one_payroll.payperiod.dto.request;

import com.tw.coupang.one_payroll.common.validator.HasPayPeriod;
import com.tw.coupang.one_payroll.common.validator.ValidPayPeriod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidPayPeriod
public class PayPeriodCreateRequest implements HasPayPeriod {

    @NotNull(message = "payGroupId is required")
    private Integer payGroupId;

    @NotNull(message = "payPeriod is required")
    @Valid
    private PayPeriod payPeriod;
}
