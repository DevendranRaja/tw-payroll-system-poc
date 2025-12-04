package com.tw.coupang.one_payroll.integration.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PayrollBatchLogResponse {
    private String batchRefId;
    private String employeeId;
    private String status;
    private String logMessage;
    private LocalDateTime timestamp;
}