package com.tw.coupang.one_payroll.integration.service;

import com.tw.coupang.one_payroll.integration.dto.PayrollBatchRequest;
import com.tw.coupang.one_payroll.integration.dto.PayrollBatchResponse;
import com.tw.coupang.one_payroll.integration.entity.PayrollBatch;
import com.tw.coupang.one_payroll.integration.entity.PayrollBatchLog;
import com.tw.coupang.one_payroll.integration.repository.PayrollBatchLogRepository;
import com.tw.coupang.one_payroll.integration.repository.PayrollBatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockIntegrationServiceTest {

    @Mock
    private PayrollBatchRepository batchRepo;

    @Mock
    private PayrollBatchLogRepository logRepo;

    @InjectMocks
    private MockIntegrationService service;

    @Test
    void shouldProcessBatchAndSaveToDb_WhenBatchIsUnique() {
        PayrollBatchRequest request = new PayrollBatchRequest();
        request.setBatchId("BATCH-NEW");
        request.setPayPeriod("2023-11");
        request.setTotalAmount(new BigDecimal("1000.00"));
        request.setEmployeeIds(List.of("E001"));

        when(batchRepo.existsByBatchId("BATCH-NEW")).thenReturn(false);

        PayrollBatchResponse response = service.processBatch(request);

        assertNotNull(response);
        assertEquals("BATCH-NEW", response.getBatchId());
        verify(batchRepo, times(2)).save(any(PayrollBatch.class));
        verify(logRepo, times(1)).save(any(PayrollBatchLog.class));
    }

    @Test
    void shouldThrowException_WhenBatchIdAlreadyExists() {
        PayrollBatchRequest request = new PayrollBatchRequest();
        request.setBatchId("BATCH-DUPLICATE");

        when(batchRepo.existsByBatchId("BATCH-DUPLICATE")).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.processBatch(request);
        });

        assertEquals("Batch ID BATCH-DUPLICATE already exists.", exception.getMessage());
        verify(batchRepo, never()).save(any(PayrollBatch.class));
    }
}
