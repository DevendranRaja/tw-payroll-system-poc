package com.tw.coupang.one_payroll.payroll.controller;

import com.tw.coupang.one_payroll.EmployeeMaster.Exception.EmployeeNotFoundException;
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
    void calculatePayrollWhenPeriodEndBeforeStartThenReturnsBadRequest() throws Exception {
        String invalidRequest = """
            {
              "employeeId": "EMP001",
              "periodStart": "2025-11-15",
              "periodEnd": "2025-11-10",
              "hoursWorked": 40
            }
        """;

        mockMvc.perform(post("/payroll/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
                .andExpect(jsonPath("$.details.periodEnd").value("periodEnd must be after periodStart"));
    }

    @Test
    void calculatePayrollWhenPeriodNotInSameMonthThenReturnsBadRequest() throws Exception {
        String invalidRequest = """
            {
              "employeeId": "EMP001",
              "periodStart": "2025-11-25",
              "periodEnd": "2025-12-01",
              "hoursWorked": 40
            }
        """;

        mockMvc.perform(post("/payroll/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
                .andExpect(jsonPath("$.details.periodStart").value("period must be within a single calendar cycle (same month)"));
    }

    @Test
    void calculatePayrollWhenValidPeriodThenCallsService() throws Exception {
        String validRequest = """
            {
              "employeeId": "EMP001",
              "periodStart": "2025-11-01",
              "periodEnd": "2025-11-30",
              "hoursWorked": 160
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
              "periodStart": "2025-11-01",
              "periodEnd": "2025-11-30",
              "hoursWorked": 160
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
    void calculatePayrollWhenHoursWorkedNegativeThenReturnsBadRequest() throws Exception {
        String invalidRequest = """
            {
              "employeeId": "EMP001",
              "periodStart": "2025-11-01",
              "periodEnd": "2025-11-30",
              "hoursWorked": -5
            }
        """;

        mockMvc.perform(post("/payroll/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.hoursWorked").exists());
    }

    @Test
    void calculatePayrollWhenEmployeeNotFoundThenReturnsNotFound() throws Exception {
        String validRequest = """
            {
              "employeeId": "EMP999",
              "periodStart": "2025-11-01",
              "periodEnd": "2025-11-30",
              "hoursWorked": 160
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
              "periodStart": "2025-11-01",
              "periodEnd": "2025-12-31",
              "hoursWorked": 160
            }
        """;

        mockMvc.perform(post("/payroll/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
                .andExpect(jsonPath("$.details.periodStart")
                        .value("period must be within a single calendar cycle (same month)"));
    }

    @Test
    void calculatePayrollWhenUnexpectedExceptionThenReturnsInternalServerError() throws Exception {
        String validRequest = """
            {
              "employeeId": "EMP001",
              "periodStart": "2025-11-01",
              "periodEnd": "2025-11-30",
              "hoursWorked": 160
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
