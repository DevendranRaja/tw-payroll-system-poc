package com.tw.coupang.one_payroll.integration.controller;

import com.tw.coupang.one_payroll.integration.dto.PayrollBatchLogResponse;
import com.tw.coupang.one_payroll.integration.service.PayrollBatchLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any; // Import for any(Class<T>) and any()
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PayrollBatchLogController.class)
@Import(com.tw.coupang.one_payroll.common.exception.GlobalExceptionHandler.class)
@DisplayName("PayrollBatchLogController Tests")
class PayrollBatchLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayrollBatchLogService payrollBatchLogService;

    private PayrollBatchLogResponse mockResponse;
    private Page<PayrollBatchLogResponse> mockPage;
    private Pageable defaultPageable = PageRequest.of(0, 20);
    private Pageable customPageable = PageRequest.of(1, 5);


    @BeforeEach
    void setUp() {
        mockResponse = PayrollBatchLogResponse.builder()
                .batchRefId("BATCH-202512")
                .employeeId("E001")
                .status("SUCCESS")
                .logMessage("Payment successful")
                .timestamp(LocalDateTime.of(2025, 12, 1, 10, 0))
                .build();

        mockPage = new PageImpl<>(List.of(mockResponse), defaultPageable, 100);
    }


    @Test
    @DisplayName("Should return 200 with paginated response when only batchId is provided (default pageable)")
    void testGetBatchLogsByBatchIdShouldReturn200() throws Exception {
        when(payrollBatchLogService.getBatchLogs(eq("BATCH-202512"), isNull(), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/integration/payroll/logs")
                        .param("batchId", "BATCH-202512"))
                .andExpect(status().isOk());

        verify(payrollBatchLogService, times(1)).getBatchLogs(eq("BATCH-202512"), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should pass custom pagination parameters to service")
    void testGetBatchLogsWithCustomPaginationShouldUseCustomPageable() throws Exception {
        Page<PayrollBatchLogResponse> customMockPage = new PageImpl<>(Collections.emptyList(), customPageable, 100);
        Pageable expectedPageable = PageRequest.of(1, 5, org.springframework.data.domain.Sort.by("timestamp"));

        // FIX: Use any(Pageable.class) in when() to avoid Mockito's IllegalArgumentException
        when(payrollBatchLogService.getBatchLogs(eq("BATCH-202512"), isNull(), any(Pageable.class)))
                .thenReturn(customMockPage);

        mockMvc.perform(get("/integration/payroll/logs")
                        .param("batchId", "BATCH-202512")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(payrollBatchLogService, times(1)).getBatchLogs(eq("BATCH-202512"), isNull(), eq(expectedPageable));
    }


    @Test
    @DisplayName("Should return 200 with paginated response when only employeeId is provided")
    void testGetBatchLogsByEmployeeIdShouldReturn200() throws Exception {
        when(payrollBatchLogService.getBatchLogs(isNull(), eq("E001"), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/integration/payroll/logs")
                        .param("employeeId", "E001"))
                .andExpect(status().isOk());

        verify(payrollBatchLogService, times(1)).getBatchLogs(isNull(), eq("E001"), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return 200 with paginated response when both batchId and employeeId are provided")
    void testGetBatchLogsByBothIdsShouldReturn200() throws Exception {
        when(payrollBatchLogService.getBatchLogs(eq("BATCH-202512"), eq("E001"), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/integration/payroll/logs")
                        .param("batchId", "BATCH-202512")
                        .param("employeeId", "E001"))
                .andExpect(status().isOk());

        verify(payrollBatchLogService, times(1)).getBatchLogs(eq("BATCH-202512"), eq("E001"), any(Pageable.class));
    }
}