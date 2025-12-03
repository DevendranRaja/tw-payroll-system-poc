package com.tw.coupang.one_payroll.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollBatchStatusResponse {
    private String batchId;
    private String overallStatus;
    private Integer numberOfEmployees;
    private LocalDateTime processedAt;
    private String logMessage;
}

