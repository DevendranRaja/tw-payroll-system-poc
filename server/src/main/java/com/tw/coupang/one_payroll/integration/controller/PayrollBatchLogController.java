package com.tw.coupang.one_payroll.integration.controller;

import com.tw.coupang.one_payroll.integration.dto.PayrollBatchLogResponse;
import com.tw.coupang.one_payroll.integration.service.PayrollBatchLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/integration/payroll")
@RequiredArgsConstructor
@Slf4j
public class PayrollBatchLogController {

    private final PayrollBatchLogService payrollBatchLogService;

    @GetMapping("/logs")
    public ResponseEntity<List<PayrollBatchLogResponse>> getBatchLogs(
            @RequestParam(required = false) String batchId,
            @RequestParam(required = false) String employeeId,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {

        log.info("GET request for batch logs. batchId: {}, employeeId: {}, Pageable: {}", batchId, employeeId, pageable);

        Page<PayrollBatchLogResponse> logs = payrollBatchLogService.getBatchLogs(batchId, employeeId, pageable);

        return ResponseEntity.ok(logs.getContent());
    }
}