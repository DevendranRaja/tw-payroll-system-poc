package com.tw.coupang.one_payroll.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PayrollBatchResponse {
    private String batchId;
    private String status;
    private String timestamp;
}
