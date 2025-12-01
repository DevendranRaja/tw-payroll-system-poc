package com.tw.coupang.one_payroll.integration.service;

import com.tw.coupang.one_payroll.integration.dto.PayrollBatchStatusResponse;
import com.tw.coupang.one_payroll.integration.entity.PayrollBatch;
import com.tw.coupang.one_payroll.integration.exception.BatchNotFoundException;
import com.tw.coupang.one_payroll.integration.repository.PayrollBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollBatchStatusService {

    private final PayrollBatchRepository payrollBatchRepository;

    public PayrollBatchStatusResponse getBatchStatus(String batchId) {
        log.info("Fetching status for batch ID: {}", batchId);

        PayrollBatch batch = payrollBatchRepository.findByBatchRefId(batchId)
                .orElseThrow(() -> {
                    log.warn("Batch not found with ID: {}", batchId);
                    return new BatchNotFoundException(batchId);
                });

        log.info("Batch found: {} with status: {}", batchId, batch.getStatus());

        return PayrollBatchStatusResponse.builder()
                .batchId(batch.getBatchRefId())
                .overallStatus(batch.getStatus())
                .numberOfEmployees(batch.getEmployeeCount())
                .processedAt(batch.getUpdatedAt())
                .logMessage(batch.getLogMessage())
                .build();
    }
}

