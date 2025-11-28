package com.tw.coupang.one_payroll.payroll.controller;

import com.tw.coupang.one_payroll.payroll.dto.request.PayPeriod;
import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.dto.response.ApiResponse;
import com.tw.coupang.one_payroll.payroll.service.PayrollCalculationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PayrollCalculationControllerTest {

    @InjectMocks
    private PayrollCalculationController payrollCalculationController;

    @Mock
    private PayrollCalculationService payrollCalculationService;

    @Test
    void calculatePayrollWithValidRequestShouldReturnOkResponse() {
        PayrollCalculationRequest request = PayrollCalculationRequest.builder()
                .employeeId("EMP123")
                .payPeriod(
                        PayPeriod.builder()
                                .startDate(LocalDate.of(2025, 1, 1))
                                .endDate(LocalDate.of(2025, 1, 31))
                                .build()
                )
                .build();

        when(payrollCalculationService.calculate(request))
                .thenReturn(null);

        ResponseEntity<ApiResponse> actual = payrollCalculationController.calculatePayroll(request);

        assertNotNull(actual);
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertNull(actual.getBody());
    }
}
