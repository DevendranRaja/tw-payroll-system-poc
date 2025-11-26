package com.tw.coupang.one_payroll.integration.service;

import com.tw.coupang.one_payroll.integration.dto.PayrollBatchRequest;
import com.tw.coupang.one_payroll.integration.dto.PayrollBatchResponse;
import com.tw.coupang.one_payroll.integration.entity.PayrollBatch;
import com.tw.coupang.one_payroll.integration.entity.PayrollBatchLog;
import com.tw.coupang.one_payroll.integration.entity.PayrollRun;
import com.tw.coupang.one_payroll.integration.enums.PayrollStatus;
import com.tw.coupang.one_payroll.integration.repository.PayrollBatchLogRepository;
import com.tw.coupang.one_payroll.integration.repository.PayrollBatchRepository;
import com.tw.coupang.one_payroll.integration.repository.PayrollRunRepository;
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

        if (batchRepo.existsByBatchRefId(request.getBatchRefId())) {
            throw new IllegalArgumentException("Batch ID " + request.getBatchRefId() + " already exists.");
        }

        // 1. Initial Persistence (Status: PENDING)
        PayrollBatch batch = PayrollBatch.builder()
                .batchRefId(request.getBatchRefId())
                .payPeriod(request.getPayPeriod())
                .totalAmount(request.getTotalAmount())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        batchRepo.save(batch);

        // 2. Determine Outcome (Mock Logic)
        String finalStatus = determineMockOutcome();

        String errorMessage = switch (finalStatus) {
            case "SUCCESS" -> "Batch accepted for processing.";
            case "RETRY" -> "Simulated Gateway Timeout (504). Please retry.";
            case "FAILED" -> "Simulated Data Validation Error. Invalid Batch.";
            default -> "Unknown status.";
        };

        // 3. Update Batch Status
        batch.setStatus(finalStatus);
        batch.setUpdatedAt(LocalDateTime.now());
        batchRepo.save(batch);

        // 4. Create a list of logs of all employees in the batch
        List<PayrollBatchLog> logs = request.getEmployeeIds().stream()
                .map(empId -> PayrollBatchLog.builder()
                        .batchRefId(request.getBatchRefId())
                        .employeeId(empId)
                        .status(finalStatus)
                        .timestamp(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        logRepo.saveAll(logs);

        return new PayrollBatchResponse(
                batch.getBatchRefId(),
                finalStatus,
                LocalDateTime.now().toString(),
                errorMessage
        );
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
