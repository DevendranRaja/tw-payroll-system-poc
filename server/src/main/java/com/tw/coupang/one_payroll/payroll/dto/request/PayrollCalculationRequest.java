package com.tw.coupang.one_payroll.payroll.dto.request;

import jakarta.validation.constraints.Min;
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

    @NotNull(message = "periodStart is required")
    private LocalDate periodStart;

    @NotNull(message = "periodEnd is required")
    private LocalDate periodEnd;

    @NotNull(message = "hoursWorked is required")
    @Min(value = 0, message = "hoursWorked cannot be negative")
    private Integer hoursWorked;
}
