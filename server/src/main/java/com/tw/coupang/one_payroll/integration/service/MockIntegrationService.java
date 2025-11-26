package com.tw.coupang.one_payroll.integration.service;

import com.tw.coupang.one_payroll.integration.dto.PayrollBatchRequest;
import com.tw.coupang.one_payroll.integration.dto.PayrollBatchResponse;
import com.tw.coupang.one_payroll.integration.entity.PayrollBatch;
import com.tw.coupang.one_payroll.integration.entity.PayrollBatchLog;
import com.tw.coupang.one_payroll.integration.repository.PayrollBatchLogRepository;
import com.tw.coupang.one_payroll.integration.repository.PayrollBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class MockIntegrationService {

    @Autowired private PayrollBatchRepository batchRepo;
    @Autowired private PayrollBatchLogRepository logRepo;

    @Transactional
    public PayrollBatchResponse processBatch(PayrollBatchRequest request) {

        if (batchRepo.existsByBatchId(request.getBatchId())) {
            throw new IllegalArgumentException("Batch ID " + request.getBatchId() + " already exists.");
        }

        // 1. Initial Persistence (Status: PENDING)
        PayrollBatch batch = PayrollBatch.builder()
                .batchId(request.getBatchId())
                .payPeriod(request.getPayPeriod())
                .totalAmount(request.getTotalAmount())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        batchRepo.save(batch);

        // 2. Determine Outcome (Mock Logic)
        String finalStatus = determineMockOutcome();

        // 3. Update Batch Status
        batch.setStatus(finalStatus);
        batch.setUpdatedAt(LocalDateTime.now());
        batchRepo.save(batch);

        // 4. Log individual employees
        request.getEmployeeIds().forEach(empId -> {
            PayrollBatchLog log = PayrollBatchLog.builder()
                    .batchId(request.getBatchId())
                    .employeeId(empId)
                    .status(finalStatus)
                    .timestamp(LocalDateTime.now())
                    .build();
            logRepo.save(log);
        });

        return new PayrollBatchResponse(
                batch.getBatchId(),
                finalStatus,
                LocalDateTime.now().toString()
        );
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
