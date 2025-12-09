package com.tw.coupang.one_payroll.payperiod.controller;

import com.tw.coupang.one_payroll.payperiod.dto.response.PayPeriodResponse;
import com.tw.coupang.one_payroll.payperiod.exception.OverlappingPayPeriodException;
import com.tw.coupang.one_payroll.payperiod.service.PayPeriodService;
import com.tw.coupang.one_payroll.userauth.config.SecurityConfig;
import com.tw.coupang.one_payroll.userauth.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PayPeriodController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        SecurityConfig.class,
                        JwtAuthenticationFilter.class
                })
        }
)
@AutoConfigureMockMvc(addFilters = false)
class PayPeriodControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PayPeriodService payPeriodService;

    @Test
    void createPayPeriodWhenStartDateAfterEndDateThenReturnsBadRequest() throws Exception {
        String requestBody = """
            {
              "payGroupId": 1,
              "payPeriod": {
                "startDate": "2025-10-31",
                "endDate": "2025-10-01"
              }
            }
        """;

        mockMvc.perform(post("/pay-periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details['payPeriod.endDate']").value("endDate must be after startDate"));
    }

    @Test
    void createPayPeriodWhenPayPeriodNotInSameMonthThenReturnsBadRequest() throws Exception {
        String requestBody = """
            {
              "payGroupId": 1,
              "payPeriod": {
                "startDate": "2025-10-28",
                "endDate": "2025-11-02"
              }
            }
        """;

        mockMvc.perform(post("/pay-periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details['payPeriod.startDate']").value(
                        "period must be within a single calendar cycle (same month)"
                ));
    }

    @Test
    void createPayPeriodWhenMissingPayGroupIdThenReturnsBadRequest() throws Exception {
        String requestBody = """
            {
              "payPeriod": {
                "startDate": "2025-10-01",
                "endDate": "2025-10-31"
              }
            }
        """;

        mockMvc.perform(post("/pay-periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.payGroupId").exists());
    }

    @Test
    void createPayPeriodWhenMissingPayPeriodThenReturnsBadRequest() throws Exception {
        String requestBody = """
            {
              "payGroupId": 1
            }
        """;

        mockMvc.perform(post("/pay-periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.payPeriod").exists());
    }

    @Test
    void createPayPeriodWhenOverlappingPeriodThenReturnsConflict() throws Exception {
        String requestBody = """
            {
              "payGroupId": 1,
              "payPeriod": {
                "startDate": "2025-10-01",
                "endDate": "2025-10-31"
              }
            }
        """;

        when(payPeriodService.create(any()))
                .thenThrow(new OverlappingPayPeriodException("Overlap detected"));

        mockMvc.perform(post("/pay-periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PAY_PERIOD_OVERLAP"))
                .andExpect(jsonPath("$.message").value("Overlap detected"));
    }

    @Test
    void createPayPeriodWhenValidRequestThenReturnsCreated() throws Exception {
        String requestBody = """
            {
              "payGroupId": 1,
              "payPeriod": {
                "startDate": "2025-10-01",
                "endDate": "2025-10-31"
              }
            }
        """;

        when(payPeriodService.create(any()))
                .thenReturn(PayPeriodResponse.builder().id(100).build());

        mockMvc.perform(post("/pay-periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("PAY_PERIOD_CREATED"))
                .andExpect(jsonPath("$.message").value("Pay period created successfully"))
                .andExpect(jsonPath("$.details.id").value(100));
    }
}
