package com.tw.coupang.one_payroll.integration.scheduler;

import com.tw.coupang.one_payroll.integration.dto.PayrollBatchRequest;
import com.tw.coupang.one_payroll.integration.dto.PayrollBatchResponse;
import com.tw.coupang.one_payroll.integration.service.MockIntegrationService;
import com.tw.coupang.one_payroll.payroll.entity.PayrollRun;
import com.tw.coupang.one_payroll.payroll.enums.PayrollStatus;
import com.tw.coupang.one_payroll.payroll.repository.PayrollRunRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollSubmissionSchedulerTest {

    @Mock
    private PayrollRunRepository payrollRunRepository;

    @Mock
    private MockIntegrationService mockIntegrationService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PayrollSubmissionScheduler scheduler;

    @Test
    void shouldSubmitBatchAndUpdateStatusWhenApiCallSucceeds() {
        // Arrange
        PayrollRun run = PayrollRun.builder()
                .employeeId("E001")
                .payPeriodEnd(LocalDate.now())
                .netPay(BigDecimal.TEN)
                .status(PayrollStatus.PROCESSED)
                .build();

        ReflectionTestUtils.setField(scheduler, "mockApiUrl", "http://localhost:8080/test-url");

        when(payrollRunRepository.findTop5ByStatusNot(PayrollStatus.SUBMITTED))
                .thenReturn(List.of(run));

        PayrollBatchResponse mockResponse = new PayrollBatchResponse("BATCH-123", "SUCCESS", "time", "Batch processed successfully.");
        when(restTemplate.postForObject(anyString(), any(PayrollBatchRequest.class), eq(PayrollBatchResponse.class)))
                .thenReturn(mockResponse);

        scheduler.submitPendingBatches();

        verify(mockIntegrationService).updateLocalRecords(anyList(), eq("SUCCESS"));
    }

    @Test
    void shouldNotUpdateRecordsWhenApiCallFails() {
        // Arrange
        PayrollRun run = PayrollRun.builder()
                .employeeId("E001")
                .payPeriodEnd(LocalDate.now())
                .netPay(BigDecimal.TEN)
                .status(PayrollStatus.PROCESSED)
                .build();

        when(payrollRunRepository.findTop5ByStatusNot(PayrollStatus.SUBMITTED))
                .thenReturn(List.of(run));

        when(restTemplate.postForObject(anyString(), any(PayrollBatchRequest.class), eq(PayrollBatchResponse.class)))
                .thenThrow(new RestClientException("Connection Refused"));

        scheduler.submitPendingBatches();

        verify(payrollRunRepository, never()).saveAll(any());
    }
}
