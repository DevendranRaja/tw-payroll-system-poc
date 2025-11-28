package com.tw.coupang.one_payroll.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tw.coupang.one_payroll.integration.dto.PayrollBatchRequest;
import com.tw.coupang.one_payroll.integration.dto.PayrollBatchResponse;
import com.tw.coupang.one_payroll.integration.service.MockIntegrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayrollIntegrationController.class)
class PayrollIntegrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MockIntegrationService mockIntegrationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn200AndResponseWhenRequestIsValid() throws Exception {
        PayrollBatchRequest request = new PayrollBatchRequest();
        request.setBatchRefId("BATCH-001");
        request.setPayPeriod("2023-10");
        request.setTotalAmount(new BigDecimal("5000.00"));
        request.setEmployeeIds(List.of("E001", "E002"));

        PayrollBatchResponse mockResponse = new PayrollBatchResponse("BATCH-001", "SUCCESS", "2023-10-01T10:00:00", "");
        when(mockIntegrationService.processBatch(any(PayrollBatchRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/integration/payroll/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batchRefId").value("BATCH-001"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        PayrollBatchRequest invalidRequest = new PayrollBatchRequest();
        invalidRequest.setBatchRefId("BATCH-001");
        invalidRequest.setTotalAmount(null);

        mockMvc.perform(post("/integration/payroll/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
