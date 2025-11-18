package com.tw.coupang.one_payroll.paygroups.dto.request;

import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PayGroupCreateRequest {

    @NotBlank(message = "groupName must not be blank")
    @Size(max = 50, message = "groupName must be <= 50 characters")
    private String groupName;

    @NotNull(message = "paymentCycle is required")
    private PaymentCycle paymentCycle;

    @NotNull(message = "baseTaxRate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "baseTaxRate must be >= 0.0")
    private BigDecimal baseTaxRate;

    @NotNull(message = "benefitRate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "benefitRate must be >= 0.0")
    private BigDecimal benefitRate;

    @NotNull(message = "deductionRate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "deductionRate must be >= 0.0")
    private BigDecimal deductionRate;
}