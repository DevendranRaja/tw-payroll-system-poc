package com.tw.coupang.one_payroll.integration.scheduler;

import com.tw.coupang.one_payroll.integration.dto.PayrollBatchRequest;
import com.tw.coupang.one_payroll.integration.dto.PayrollBatchResponse;
import com.tw.coupang.one_payroll.integration.entity.PayrollRun;
import com.tw.coupang.one_payroll.integration.enums.PayrollStatus;
import com.tw.coupang.one_payroll.integration.repository.PayrollRunRepository;
import com.tw.coupang.one_payroll.integration.service.MockIntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
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
    private MockIntegrationService mockIntegrationService;

    @Value("${integration.payroll.submit-url}")
    private String mockApiUrl;

    public PayrollSubmissionScheduler(PayrollRunRepository payrollRunRepository, RestTemplate restTemplate, MockIntegrationService mockIntegrationService) {
        this.payrollRunRepository = payrollRunRepository;
        this.restTemplate = restTemplate;
        this.mockIntegrationService = mockIntegrationService;
    }

    // Runs every minute for the PoC to demonstrate the flow
    @Scheduled(fixedDelay = 60000)
    public void submitPendingBatches() {
        log.info("Checking for pending payroll records...");

        // 1. Fetch Chunk as bulk rows of 100
        List<PayrollRun> pendingRuns = payrollRunRepository.findTop5ByStatus(PayrollStatus.PROCESSED);

        if (pendingRuns.isEmpty()) {
            log.info("No pending payroll records found.");
            return;
        }

        log.info("Found {} records. Preparing batch...", pendingRuns.size());

        try {
            // 2. Aggregate Data into Request DTO
            PayrollBatchRequest batchRequest = createBatchRequest(pendingRuns);

            // 3. Call the Mock API
            log.info("Sending Batch ID: {} to Mock SAP...", batchRequest.getBatchRefId());
            PayrollBatchResponse response = restTemplate.postForObject(mockApiUrl, batchRequest, PayrollBatchResponse.class);

            // 4. Update Local Database based on Response
            if (response != null) {
                log.info("Response received: Status={}, Message={}", response.getStatus(), response.getErrorMessage());
                mockIntegrationService.updateLocalRecords(pendingRuns, response.getStatus());
            }

        } catch (Exception e) {
            log.error("Failed to submit batch: {}", e.getMessage());
        }
    }

    private PayrollBatchRequest createBatchRequest(List<PayrollRun> runs) {
        PayrollBatchRequest request = new PayrollBatchRequest();

        // Generate a unique Batch ID
        request.setBatchRefId("BATCH-" + UUID.randomUUID().toString().substring(0, 8));

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
}
