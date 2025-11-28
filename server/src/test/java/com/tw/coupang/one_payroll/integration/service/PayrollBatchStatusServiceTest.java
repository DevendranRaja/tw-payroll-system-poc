package com.tw.coupang.one_payroll.integration.service;

import com.tw.coupang.one_payroll.integration.dto.PayrollBatchStatusResponse;
import com.tw.coupang.one_payroll.integration.entity.PayrollBatch;
import com.tw.coupang.one_payroll.integration.exception.BatchNotFoundException;
import com.tw.coupang.one_payroll.integration.repository.PayrollBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayrollBatchStatusService Tests")
class PayrollBatchStatusServiceTest {

    @Mock
    private PayrollBatchRepository payrollBatchRepository;

    @InjectMocks
    private PayrollBatchStatusService payrollBatchStatusService;

    private PayrollBatch testBatch;

    @BeforeEach
    void setUp() {
        testBatch = PayrollBatch.builder()
                .id(1L)
                .batchRefId("BATCH-20251128-001")
                .status("SUCCESS")
                .employeeCount(100)
                .logMessage("Batch processed successfully")
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return batch status when batch exists")
    void testGetBatchStatusWhenBatchExistsShouldReturnResponse() {
        String batchId = "BATCH-20251128-001";
        when(payrollBatchRepository.findByBatchRefId(batchId)).thenReturn(Optional.of(testBatch));

        PayrollBatchStatusResponse response = payrollBatchStatusService.getBatchStatus(batchId);

        assertNotNull(response);
        assertEquals("BATCH-20251128-001", response.getBatchId());
        assertEquals("SUCCESS", response.getOverallStatus());
        assertEquals(100, response.getNumberOfEmployees());
        assertEquals("Batch processed successfully", response.getLogsMessage());
        assertNotNull(response.getProcessedAt());

        verify(payrollBatchRepository, times(1)).findByBatchRefId(batchId);
    }

    @Test
    @DisplayName("Should throw BatchNotFoundException when batch does not exist")
    void testGetBatchStatusWhenBatchDoesNotExistShouldThrowException() {
        String batchId = "BATCH-99999999-999";
        when(payrollBatchRepository.findByBatchRefId(batchId)).thenReturn(Optional.empty());

        assertThrows(BatchNotFoundException.class, () -> {
            payrollBatchStatusService.getBatchStatus(batchId);
        });

        verify(payrollBatchRepository, times(1)).findByBatchRefId(batchId);
    }

    @Test
    @DisplayName("Should return correct batch status with all fields populated")
    void testGetBatchStatusShouldReturnAllFieldsCorrectly() {
        testBatch.setStatus("PENDING");
        testBatch.setEmployeeCount(50);
        testBatch.setLogMessage("Awaiting processing");

        when(payrollBatchRepository.findByBatchRefId("BATCH-20251128-001"))
                .thenReturn(Optional.of(testBatch));

        PayrollBatchStatusResponse response = payrollBatchStatusService.getBatchStatus("BATCH-20251128-001");

        assertEquals("PENDING", response.getOverallStatus());
        assertEquals(50, response.getNumberOfEmployees());
        assertEquals("Awaiting processing", response.getLogsMessage());
    }

    @Test
    @DisplayName("Should handle null logMessage gracefully")
    void testGetBatchStatusWithNullLogMessageShouldReturnNull() {
        testBatch.setLogMessage(null);
        when(payrollBatchRepository.findByBatchRefId("BATCH-20251128-001"))
                .thenReturn(Optional.of(testBatch));

        PayrollBatchStatusResponse response = payrollBatchStatusService.getBatchStatus("BATCH-20251128-001");

        assertNull(response.getLogsMessage());
    }

    @Test
    @DisplayName("Should handle different batch statuses")
    void testGetBatchStatusWithDifferentStatuses() {
        testBatch.setStatus("FAILED");
        when(payrollBatchRepository.findByBatchRefId("BATCH-20251128-001"))
                .thenReturn(Optional.of(testBatch));

        PayrollBatchStatusResponse response = payrollBatchStatusService.getBatchStatus("BATCH-20251128-001");
        assertEquals("FAILED", response.getOverallStatus());

        testBatch.setStatus("RETRY");
        response = payrollBatchStatusService.getBatchStatus("BATCH-20251128-001");
        assertEquals("RETRY", response.getOverallStatus());
    }

    @Test
    @DisplayName("BatchNotFoundException should have correct message")
    void testBatchNotFoundExceptionMessageFormat() {
        String batchId = "BATCH-INVALID";
        when(payrollBatchRepository.findByBatchRefId(batchId)).thenReturn(Optional.empty());
        
        BatchNotFoundException exception = assertThrows(BatchNotFoundException.class, () -> {
            payrollBatchStatusService.getBatchStatus(batchId);
        });

        assertTrue(exception.getMessage().contains(batchId));
        assertTrue(exception.getMessage().contains("Batch not found"));
    }
}

