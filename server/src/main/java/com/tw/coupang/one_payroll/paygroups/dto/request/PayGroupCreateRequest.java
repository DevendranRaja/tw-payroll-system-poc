package com.tw.coupang.one_payroll.paygroups.dto.request;

import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayGroupCreateRequest {

    @NotBlank
    private String groupName;

    @NotNull
    private PaymentCycle paymentCycle; // WEEKLY | BIWEEKLY | MONTHLY

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal baseTaxRate;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal benefitRate;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal deductionRate;
}

