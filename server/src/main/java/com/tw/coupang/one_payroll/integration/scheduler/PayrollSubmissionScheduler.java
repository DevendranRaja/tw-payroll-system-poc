package com.tw.coupang.one_payroll.integration.scheduler;

import com.tw.coupang.one_payroll.integration.dto.PayrollBatchRequest;
import com.tw.coupang.one_payroll.integration.dto.PayrollBatchResponse;
import com.tw.coupang.one_payroll.integration.entity.PayrollRun;
import com.tw.coupang.one_payroll.integration.enums.PayrollStatus;
import com.tw.coupang.one_payroll.integration.repository.PayrollRunRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PayrollSubmissionScheduler {

    private PayrollRunRepository payrollRunRepository;
    private RestTemplate restTemplate;
    private final String MOCK_API_URL = "http://localhost:8080/tw-payroll-system/api/integration/payroll/submit";

    public PayrollSubmissionScheduler(PayrollRunRepository payrollRunRepository, RestTemplate restTemplate) {
        this.payrollRunRepository = payrollRunRepository;
        this.restTemplate = restTemplate;
    }

    // Runs every minute for the PoC to demonstrate the flow
    @Scheduled(fixedDelay = 60000)
    public void submitPendingBatches() {
        log.info("Checking for pending payroll records...");

        // 1. Fetch Chunk as bulk rows of 100
        List<PayrollRun> pendingRuns = payrollRunRepository.findTop100ByStatus(PayrollStatus.PROCESSED);

        if (pendingRuns.isEmpty()) {
            log.info("No pending payroll records found.");
            return;
        }

        log.info("Found {} records. Preparing batch...", pendingRuns.size());

        try {
            // 2. Aggregate Data into Request DTO
            PayrollBatchRequest batchRequest = createBatchRequest(pendingRuns);

            // 3. Call the Mock API
            log.info("Sending Batch ID: {} to Mock SAP...", batchRequest.getBatchId());
            PayrollBatchResponse response = restTemplate.postForObject(MOCK_API_URL, batchRequest, PayrollBatchResponse.class);

            // 4. Update Local Database based on Response
            if (response != null) {
                updateLocalRecords(pendingRuns, response.getStatus());
            }

        } catch (Exception e) {
            log.error("Failed to submit batch: {}", e.getMessage());
        }
    }

    private PayrollBatchRequest createBatchRequest(List<PayrollRun> runs) {
        PayrollBatchRequest request = new PayrollBatchRequest();

        // Generate a unique Batch ID
        request.setBatchId("BATCH-" + UUID.randomUUID().toString().substring(0, 8));

        // Derive Pay Period from the first record and Format YYYY-MM
        String payPeriod = runs.get(0).getPayPeriodEnd().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        request.setPayPeriod(payPeriod);

        // Map Employee IDs
        List<String> employeeIds = runs.stream()
                .map(PayrollRun::getEmployeeId)
                .collect(Collectors.toList());
        request.setEmployeeIds(employeeIds);

        // Sum Total Amount
        BigDecimal totalAmount = runs.stream()
                .map(PayrollRun::getNetPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        request.setTotalAmount(totalAmount);

        return request;
    }

    private void updateLocalRecords(List<PayrollRun> runs, String responseStatus) {
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
}
