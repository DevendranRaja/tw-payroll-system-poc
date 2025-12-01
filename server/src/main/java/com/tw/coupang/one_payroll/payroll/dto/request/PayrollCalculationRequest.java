package com.tw.coupang.one_payroll.payroll.dto.request;

import com.tw.coupang.one_payroll.payroll.validator.ValidPayPeriod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ValidPayPeriod
public class PayrollCalculationRequest {

    @NotBlank(message = "employeeId is required")
    private String employeeId;

    @NotNull(message = "payPeriod is required")
    @Valid
    private PayPeriod payPeriod;
}
