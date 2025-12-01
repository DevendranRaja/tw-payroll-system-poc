package com.tw.coupang.one_payroll.payroll.controller;

import com.tw.coupang.one_payroll.employee_master.exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.payroll.dto.response.ApiResponse;
import com.tw.coupang.one_payroll.payroll.service.PayrollCalculationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayrollCalculationController.class)
class PayrollCalculationControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PayrollCalculationService payrollCalculationService;

    @Test
    void calculatePayrollWhenPayPeriodEndDateBeforeStartDateThenReturnsBadRequest() throws Exception {
        String invalidRequest = """
            {
              "employeeId": "EMP001",
              "payPeriod": {
                "startDate": "2025-11-15",
                "endDate": "2025-11-10"
              }
            }
        """;

        mockMvc.perform(post("/payroll/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
                .andExpect(jsonPath("$.details['payPeriod.endDate']").value("endDate must be after startDate"));
    }

    @Test
    void calculatePayrollWhenPeriodNotInSameMonthThenReturnsBadRequest() throws Exception {
        String invalidRequest = """
            {
              "employeeId": "EMP001",
              "payPeriod": {
                "startDate": "2025-11-25",
                "endDate": "2025-12-01"
              }
            }
        """;

        mockMvc.perform(post("/payroll/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
                .andExpect(jsonPath("$.details['payPeriod.startDate']").value("period must be within a single calendar cycle (same month)"));
    }

    @Test
    void calculatePayrollWhenValidPeriodThenCallsService() throws Exception {
        String validRequest = """
            {
              "employeeId": "EMP001",
              "payPeriod": {
                "startDate": "2025-11-01",
                "endDate": "2025-11-30"
              }
            }
        """;

        when(payrollCalculationService.calculate(any())).thenReturn(
                ApiResponse.success("PAYROLL_CALCULATION_SUCCESS", "Payroll calculation completed successfully", null)
        );

        mockMvc.perform(post("/payroll/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PAYROLL_CALCULATION_SUCCESS"))
                .andExpect(jsonPath("$.message").value("Payroll calculation completed successfully"));
    }

    @Test
    void calculatePayrollWhenMissingEmployeeIdThenReturnsBadRequest() throws Exception {
        String invalidRequest = """
            {
              "payPeriod": {
                "startDate": "2025-11-01",
                "endDate": "2025-11-30"
              }
            }
        """;

        mockMvc.perform(post("/payroll/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.employeeId").exists());
    }

    @Test
    void calculatePayrollWhenMissingPayPeriodThenReturnsBadRequest() throws Exception {
        String invalidRequest = """
            {
              "employeeId": "EMP001"
            }
        """;

        mockMvc.perform(post("/payroll/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.payPeriod").exists());
    }

    @Test
    void calculatePayrollWhenEmployeeNotFoundThenReturnsNotFound() throws Exception {
        String validRequest = """
            {
              "employeeId": "EMP999",
              "payPeriod": {
                "startDate": "2025-11-01",
                "endDate": "2025-11-30"
              }
            }
        """;

        when(payrollCalculationService.calculate(any()))
                .thenThrow(new EmployeeNotFoundException("Employee not found"));

        mockMvc.perform(post("/payroll/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    void calculatePayrollWhenInvalidPayPeriodThenReturnsBadRequest() throws Exception {
        String invalidRequest = """
            {
              "employeeId": "EMP001",
              "payPeriod": {
                "startDate": "2025-11-01",
                "endDate": "2025-11-31"
              }
            }
        """;

        mockMvc.perform(post("/payroll/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid request format."))
                .andExpect(jsonPath("$.details").value("Invalid date 'NOVEMBER 31'"));
    }

    @Test
    void calculatePayrollWhenUnexpectedExceptionThenReturnsInternalServerError() throws Exception {
        String validRequest = """
            {
              "employeeId": "EMP001",
              "payPeriod": {
                "startDate": "2025-11-01",
                "endDate": "2025-11-30"
              }
            }
        """;

        when(payrollCalculationService.calculate(any()))
                .thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(post("/payroll/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message")
                        .value("An unexpected error occurred. Please try again later."));
    }
}
