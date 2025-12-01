package com.tw.coupang.one_payroll.integration.service;

import com.tw.coupang.one_payroll.integration.dto.PayrollBatchRequest;
import com.tw.coupang.one_payroll.integration.dto.PayrollBatchResponse;
import com.tw.coupang.one_payroll.integration.entity.PayrollBatch;
import com.tw.coupang.one_payroll.integration.entity.PayrollBatchLog;
import com.tw.coupang.one_payroll.integration.repository.PayrollBatchLogRepository;
import com.tw.coupang.one_payroll.integration.repository.PayrollBatchRepository;
import com.tw.coupang.one_payroll.payroll.entity.PayrollRun;
import com.tw.coupang.one_payroll.payroll.enums.PayrollStatus;
import com.tw.coupang.one_payroll.payroll.repository.PayrollRunRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MockIntegrationService {

    private PayrollBatchRepository batchRepo;
    private PayrollBatchLogRepository logRepo;
    private PayrollRunRepository payrollRunRepository;

    public MockIntegrationService(PayrollBatchRepository batchRepo, PayrollBatchLogRepository logRepo, PayrollRunRepository payrollRunRepository) {
        this.batchRepo = batchRepo;
        this.logRepo = logRepo;
        this.payrollRunRepository = payrollRunRepository;
    }

    @Transactional
    public PayrollBatchResponse processBatch(PayrollBatchRequest request) {
        // Validation
        validateBatchUniqueness(request.getBatchRefId());

        // Extract Initial Save
        PayrollBatch batch = initializeBatch(request);

        // Extract Processing and Final Update
        return finalizeBatchProcessing(batch, request.getEmployeeIds());
    }

    //Handles Validation
    private void validateBatchUniqueness(String batchRefId) {
        if (batchRepo.existsByBatchRefId(batchRefId)) {
            throw new IllegalArgumentException("Batch ID " + batchRefId + " already exists.");
        }
    }

    //Handles the Creation of the PENDING state
    private PayrollBatch initializeBatch(PayrollBatchRequest request) {
        PayrollBatch batch = PayrollBatch.builder()
                .batchRefId(request.getBatchRefId())
                .payPeriod(request.getPayPeriod())
                .totalAmount(request.getTotalAmount())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return batchRepo.save(batch);
    }

    //Handles the Mock Logic, Updates, and Logs
    private PayrollBatchResponse finalizeBatchProcessing(PayrollBatch batch, List<String> employeeIds) {
        //Determine Outcome
        String finalStatus = determineMockOutcome();
        String errorMessage = getStatusMessage(finalStatus);

        //Update Batch
        batch.setStatus(finalStatus);
        batch.setUpdatedAt(LocalDateTime.now());
        batchRepo.save(batch);

        //Save Logs
        saveBatchLogs(batch.getBatchRefId(), employeeIds, finalStatus);

        //Return Response
        return new PayrollBatchResponse(
                batch.getBatchRefId(),
                finalStatus,
                LocalDateTime.now().toString(),
                errorMessage
        );
    }

    private void saveBatchLogs(String batchRefId, List<String> employeeIds, String status) {
        List<PayrollBatchLog> logs = employeeIds.stream()
                .map(empId -> PayrollBatchLog.builder()
                        .batchRefId(batchRefId)
                        .employeeId(empId)
                        .status(status)
                        .timestamp(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
        logRepo.saveAll(logs);
    }

    private String getStatusMessage(String status) {
        return switch (status) {
            case "SUCCESS" -> "Batch accepted for processing.";
            case "RETRY" -> "Simulated Gateway Timeout (504). Please retry.";
            case "FAILED" -> "Simulated Data Validation Error. Invalid Batch.";
            default -> "Unknown status.";
        };
    }

    @Transactional
    public void updateLocalRecords(List<PayrollRun> runs, String responseStatus) {
        PayrollStatus newStatus;

        // Map Mock API string status to our internal Enum
        switch (responseStatus) {
            case "SUCCESS":
                newStatus = PayrollStatus.SUBMITTED;
                break;
            case "RETRY":
                // retry mechanism can be implemented here
                log.info("Batch marked for RETRY. Leaving records as PROCESSED.");
                return;
            case "FAILED":
                newStatus = PayrollStatus.SUBMISSION_FAILED;
                break;
            default:
                newStatus = PayrollStatus.SUBMISSION_FAILED;
        }

        // Update all records in this chunk
        runs.forEach(run -> run.setStatus(newStatus));
        payrollRunRepository.saveAll(runs);

        log.info("Updated {} records to status: {}", runs.size(), newStatus);
    }

    private String determineMockOutcome() {
        double chance = Math.random(); // Returns 0.0 to 1.0

        if (chance < 0.80) {
            return "SUCCESS";          // 0% - 80%
        } else if (chance < 0.90) {
            return "RETRY";            // 80% - 90% (Temporary Failure)
        } else {
            return "FAILED";           // 90% - 100% (Permanent Failure)
        }
    }
}
