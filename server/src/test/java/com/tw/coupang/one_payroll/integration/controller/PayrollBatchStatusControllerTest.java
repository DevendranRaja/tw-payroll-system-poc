package com.tw.coupang.one_payroll.integration.controller;

import com.tw.coupang.one_payroll.common.exception.GlobalExceptionHandler;
import com.tw.coupang.one_payroll.integration.dto.PayrollBatchStatusResponse;
import com.tw.coupang.one_payroll.integration.exception.BatchNotFoundException;
import com.tw.coupang.one_payroll.integration.service.PayrollBatchStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayrollBatchStatusController Tests")
class PayrollBatchStatusControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PayrollBatchStatusService payrollBatchStatusService;

    @InjectMocks
    private PayrollBatchStatusController payrollBatchStatusController;

    private PayrollBatchStatusResponse testResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(payrollBatchStatusController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testResponse = PayrollBatchStatusResponse.builder()
                .batchId("BATCH-20251128-001")
                .overallStatus("SUCCESS")
                .numberOfEmployees(100)
                .processedAt(LocalDateTime.now())
                .logsMessage("Batch processed successfully")
                .build();
    }

    @Test
    @DisplayName("Should return 200 with batch status when batch exists")
    void testGetBatchStatusWhenBatchExistsShouldReturn200() throws Exception {
        String batchId = "BATCH-20251128-001";
        when(payrollBatchStatusService.getBatchStatus(batchId)).thenReturn(testResponse);

        mockMvc.perform(get("/integration/payroll/status/{batchId}", batchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batchId", is("BATCH-20251128-001")))
                .andExpect(jsonPath("$.overallStatus", is("SUCCESS")))
                .andExpect(jsonPath("$.numberOfEmployees", is(100)))
                .andExpect(jsonPath("$.logsMessage", is("Batch processed successfully")))
                .andExpect(jsonPath("$.processedAt", notNullValue()));

        verify(payrollBatchStatusService, times(1)).getBatchStatus(batchId);
    }

    @Test
    @DisplayName("Should return 404 when batch does not exist")
    void testGetBatchStatusWhenBatchDoesNotExistShouldReturn404() throws Exception {

        String batchId = "BATCH-99999999-999";
        when(payrollBatchStatusService.getBatchStatus(batchId))
                .thenThrow(new BatchNotFoundException(batchId));


        mockMvc.perform(get("/integration/payroll/status/{batchId}", batchId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Batch not found")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        verify(payrollBatchStatusService, times(1)).getBatchStatus(batchId);
    }

    @Test
    @DisplayName("Should return correct batch status with PENDING status")
    void testGetBatchStatusWithPendingStatusShouldReturn200() throws Exception {

        testResponse.setOverallStatus("PENDING");
        testResponse.setLogsMessage("Awaiting processing");
        when(payrollBatchStatusService.getBatchStatus("BATCH-20251128-001"))
                .thenReturn(testResponse);


        mockMvc.perform(get("/integration/payroll/status/{batchId}", "BATCH-20251128-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallStatus", is("PENDING")))
                .andExpect(jsonPath("$.logsMessage", is("Awaiting processing")));
    }

    @Test
    @DisplayName("Should return correct batch status with FAILED status")
    void testGetBatchStatusWithFailedStatusShouldReturn200() throws Exception {

        testResponse.setOverallStatus("FAILED");
        testResponse.setLogsMessage("Processing failed due to validation error");
        when(payrollBatchStatusService.getBatchStatus("BATCH-20251128-001"))
                .thenReturn(testResponse);


        mockMvc.perform(get("/integration/payroll/status/{batchId}", "BATCH-20251128-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallStatus", is("FAILED")))
                .andExpect(jsonPath("$.logsMessage", containsString("failed")));
    }

    @Test
    @DisplayName("Should return 404 with error details when batch not found")
    void testGetBatchStatusNotFoundShouldReturnErrorDetails() throws Exception {

        String batchId = "BATCH-INVALID-ID";
        when(payrollBatchStatusService.getBatchStatus(batchId))
                .thenThrow(new BatchNotFoundException(batchId));


        mockMvc.perform(get("/integration/payroll/status/{batchId}", batchId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString(batchId)));
    }

    @Test
    @DisplayName("Should handle batch with null logMessage")
    void testGetBatchStatusWithNullLogMessageShouldReturn200() throws Exception {

        testResponse.setLogsMessage(null);
        when(payrollBatchStatusService.getBatchStatus("BATCH-20251128-001"))
                .thenReturn(testResponse);

        mockMvc.perform(get("/integration/payroll/status/{batchId}", "BATCH-20251128-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logsMessage", nullValue()));
    }

    @Test
    @DisplayName("Should handle batch with zero employees")
    void testGetBatchStatusWithZeroEmployeesShouldReturn200() throws Exception {

        testResponse.setNumberOfEmployees(0);
        when(payrollBatchStatusService.getBatchStatus("BATCH-20251128-001"))
                .thenReturn(testResponse);

        mockMvc.perform(get("/integration/payroll/status/{batchId}", "BATCH-20251128-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfEmployees", is(0)));
    }

    @Test
    @DisplayName("Should handle different batchId formats")
    void testGetBatchStatusWithDifferentBatchIdFormats() throws Exception {
        testResponse.setBatchId("BATCH-20251127-100");
        when(payrollBatchStatusService.getBatchStatus("BATCH-20251127-100"))
                .thenReturn(testResponse);

        mockMvc.perform(get("/integration/payroll/status/{batchId}", "BATCH-20251127-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batchId", is("BATCH-20251127-100")));
    }
}
