package com.tw.coupang.one_payroll.integration.service;

import com.tw.coupang.one_payroll.integration.dto.PayrollBatchLogResponse;
import com.tw.coupang.one_payroll.integration.entity.PayrollBatchLog;
import com.tw.coupang.one_payroll.integration.exception.MandatoryFieldMissingException;
import com.tw.coupang.one_payroll.integration.repository.PayrollBatchLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayrollBatchLogService Tests")
class PayrollBatchLogServiceTest {

    @Mock
    private PayrollBatchLogRepository payrollBatchLogRepository;

    @InjectMocks
    private PayrollBatchLogService payrollBatchLogService;

    private PayrollBatchLog mockLog;
    private Pageable mockPageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        mockLog = PayrollBatchLog.builder()
                .logId(1L)
                .batchRefId("BATCH-202511")
                .employeeId("E001")
                .status("SUCCESS")
                .logMessage("Processed OK")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should throw exception when both batchId and employeeId are null/empty")
    void testGetBatchLogsMandatoryFieldsMissing() {
        MandatoryFieldMissingException exception = assertThrows(MandatoryFieldMissingException.class, () -> {
            payrollBatchLogService.getBatchLogs(null, "", mockPageable);
        });

        assertTrue(exception.getMessage().contains("batchId or employeeId"));
        verifyNoInteractions(payrollBatchLogRepository);
    }

    @Test
    @DisplayName("Should fetch paginated logs by batchId only")
    void testGetBatchLogsByBatchIdOnly() {
        Page<PayrollBatchLog> mockPage = new PageImpl<>(List.of(mockLog), mockPageable, 1);
        when(payrollBatchLogRepository.findByBatchRefId("BATCH-202511", mockPageable)).thenReturn(mockPage);

        Page<PayrollBatchLogResponse> result = payrollBatchLogService.getBatchLogs("BATCH-202511", null, mockPageable);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals("E001", result.getContent().get(0).getEmployeeId());
        verify(payrollBatchLogRepository, times(1)).findByBatchRefId("BATCH-202511", mockPageable);
        verify(payrollBatchLogRepository, never()).findByEmployeeId(anyString(), any());
        verify(payrollBatchLogRepository, never()).findByBatchRefIdAndEmployeeId(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Should fetch paginated logs by employeeId only")
    void testGetBatchLogsByEmployeeIdOnly() {
        Page<PayrollBatchLog> mockPage = new PageImpl<>(List.of(mockLog), mockPageable, 1);
        when(payrollBatchLogRepository.findByEmployeeId("E001", mockPageable)).thenReturn(mockPage);

        Page<PayrollBatchLogResponse> result = payrollBatchLogService.getBatchLogs(null, "E001", mockPageable);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals("BATCH-202511", result.getContent().get(0).getBatchRefId());
        verify(payrollBatchLogRepository, times(1)).findByEmployeeId("E001", mockPageable);
        verify(payrollBatchLogRepository, never()).findByBatchRefId(anyString(), any());
        verify(payrollBatchLogRepository, never()).findByBatchRefIdAndEmployeeId(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Should fetch paginated logs by both batchId and employeeId")
    void testGetBatchLogsByBothIds() {
        Page<PayrollBatchLog> mockPage = new PageImpl<>(List.of(mockLog), mockPageable, 1);
        when(payrollBatchLogRepository.findByBatchRefIdAndEmployeeId("BATCH-202511", "E001", mockPageable)).thenReturn(mockPage);

        Page<PayrollBatchLogResponse> result = payrollBatchLogService.getBatchLogs("BATCH-202511", "E001", mockPageable);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals("SUCCESS", result.getContent().get(0).getStatus());
        verify(payrollBatchLogRepository, times(1)).findByBatchRefIdAndEmployeeId("BATCH-202511", "E001", mockPageable);
        verify(payrollBatchLogRepository, never()).findByBatchRefId(anyString(), any());
        verify(payrollBatchLogRepository, never()).findByEmployeeId(anyString(), any());
    }

    @Test
    @DisplayName("Should return empty page when no records are found")
    void testGetBatchLogsNoRecordsFound() {
        Page<PayrollBatchLog> mockEmptyPage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);
        when(payrollBatchLogRepository.findByBatchRefId(anyString(), any())).thenReturn(mockEmptyPage);

        Page<PayrollBatchLogResponse> result = payrollBatchLogService.getBatchLogs("BATCH-MISSING", null, mockPageable);

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }
}