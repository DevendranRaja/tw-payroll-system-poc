package com.tw.coupang.one_payroll.paygroups.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
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
public class PayGroupUpdateRequest {

    @Size(min = 1, max = 50, message = "groupName must not be empty and must be <= 50 characters")
    @Pattern(regexp = "^\\S+$", message = "groupName must not contain spaces")
    private String groupName;

    private PaymentCycle paymentCycle;

    @DecimalMin(value = "0.0", inclusive = true, message = "baseTaxRate must be >= 0.0")
    @DecimalMax(value = "60.0", inclusive = true, message = "baseTaxRate must be <= 60.0")
    @JsonSetter(nulls = Nulls.FAIL)
    private BigDecimal baseTaxRate;

    @DecimalMin(value = "0.0", inclusive = true, message = "benefitRate must be >= 0.0")
    @DecimalMax(value = "100.0", inclusive = true, message = "benefitRate must be <= 100.0")
    @JsonSetter(nulls = Nulls.FAIL)
    private BigDecimal benefitRate;

    @DecimalMin(value = "0.0", inclusive = true, message = "deductionRate must be >= 0.0")
    @DecimalMax(value = "100.0", inclusive = true, message = "deductionRate must be <= 100.0")
    @JsonSetter(nulls = Nulls.FAIL)
    private BigDecimal deductionRate;
}
