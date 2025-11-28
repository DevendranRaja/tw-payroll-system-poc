package com.tw.coupang.one_payroll.integration.controller;

import com.tw.coupang.one_payroll.integration.dto.PayrollBatchRequest;
import com.tw.coupang.one_payroll.integration.dto.PayrollBatchResponse;
import com.tw.coupang.one_payroll.integration.service.MockIntegrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/integration/payroll")
public class PayrollIntegrationController {

    private MockIntegrationService integrationService;

    public PayrollIntegrationController(MockIntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @PostMapping("/submit")
    public ResponseEntity<PayrollBatchResponse> submitPayrollBatch(@Valid @RequestBody PayrollBatchRequest request) {

        PayrollBatchResponse response = integrationService.processBatch(request);
        return ResponseEntity.ok(response);
    }
}
