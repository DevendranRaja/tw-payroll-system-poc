package com.tw.coupang.one_payroll.payroll.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PayrollCalculationRequest {

    @NotBlank(message = "employeeId is required")
    private String employeeId;

    @NotNull(message = "payPeriod is required")
    private LocalDate payPeriodStart;

    @NotNull(message = "payPeriod is required")
    private LocalDate payPeriodEnd;
}
