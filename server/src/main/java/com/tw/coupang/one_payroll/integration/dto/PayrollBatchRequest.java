package com.tw.coupang.one_payroll.integration.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PayrollBatchRequest {

    @NotBlank(message = "Batch ID is required")
    private String batchId;

    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Pay period must be YYYY-MM")
    private String payPeriod;

    @NotEmpty(message = "Employee list cannot be empty")
    private List<String> employeeIds;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;
}
