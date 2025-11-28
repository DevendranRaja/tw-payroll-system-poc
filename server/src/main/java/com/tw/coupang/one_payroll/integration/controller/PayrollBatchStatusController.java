package com.tw.coupang.one_payroll.integration.controller;

import com.tw.coupang.one_payroll.integration.dto.PayrollBatchStatusResponse;
import com.tw.coupang.one_payroll.integration.service.PayrollBatchStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/integration/payroll")
@RequiredArgsConstructor
@Slf4j
public class PayrollBatchStatusController {

    private final PayrollBatchStatusService payrollBatchStatusService;


    @GetMapping("/status/{batchId}")
    public ResponseEntity<PayrollBatchStatusResponse> getBatchStatus(@PathVariable String batchId) {
        log.info("GET request for batch status: {}", batchId);
        PayrollBatchStatusResponse response = payrollBatchStatusService.getBatchStatus(batchId);
        return ResponseEntity.ok(response);
    }
}

