package com.tw.coupang.one_payroll.payroll.controller;

import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.dto.response.ApiResponse;
import com.tw.coupang.one_payroll.payroll.dto.response.PayrollRunResponse;
import com.tw.coupang.one_payroll.payroll.entity.PayrollRun;
import com.tw.coupang.one_payroll.payroll.service.PayrollCalculationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static java.math.BigDecimal.valueOf;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
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
                .payPeriodStart(LocalDate.of(2025, 1, 1))
                .payPeriodEnd(LocalDate.of(2025, 1, 31))
                .build();

        PayrollRunResponse payrollRunResponse = PayrollRunResponse.builder().employeeId(request.getEmployeeId())
                .payPeriodStart(request.getPayPeriodStart()).payPeriodEnd(request.getPayPeriodEnd()).build();
        when(payrollCalculationService.calculate(request)).thenReturn(payrollRunResponse);

        ResponseEntity<ApiResponse> actual = payrollCalculationController.calculatePayroll(request);

        assertNotNull(actual);
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals("PAYROLL_CALCULATION_SUCCESS", actual.getBody().getCode());
        assertEquals("Payroll calculation completed successfully", actual.getBody().getMessage());
        assertEquals(payrollRunResponse, actual.getBody().getDetails());
        assertNotNull(actual.getBody().getTimestamp());
    }

    @Test
    void getPayrollByEmployeeIdShouldReturnPayrollRunDataWithOkResponse() {
        //given
        String employeeId = "EMP123";
        LocalDate periodStart = LocalDate.of(2025, 1, 1);
        LocalDate periodEnd = LocalDate.of(2025, 1, 31);
        final var payrollRun = getPayrollRunResponse(employeeId, periodStart, periodEnd);

        when(payrollCalculationService.getPayroll(employeeId, null, null))
                .thenReturn(singletonList(payrollRun));

        //when
        ResponseEntity<ApiResponse> actual = payrollCalculationController.getPayroll(employeeId, null, null);

        //then
        assertNotNull(actual);
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        final var listOfPayrollRuns = (List<PayrollRun>) actual.getBody().getDetails();
        assertEquals(1, listOfPayrollRuns.size());
        assertEquals(payrollRun, listOfPayrollRuns.get(0));
    }

    @Test
    void getPayrollByPeriodStartAndPeriodEndShouldReturnPayrollRunDataWithOkResponse() {
        //given
        String employeeId = "EMP123";
        LocalDate periodStart = LocalDate.of(2025, 1, 1);
        LocalDate periodEnd = LocalDate.of(2025, 1, 31);
        final var payrollRun = getPayrollRunResponse(employeeId, periodStart, periodEnd);

        when(payrollCalculationService.getPayroll(employeeId, periodStart, periodEnd))
                .thenReturn(singletonList(payrollRun));

        //when
        ResponseEntity<ApiResponse> actual = payrollCalculationController.getPayroll(employeeId, periodStart, periodEnd);

        //then
        assertNotNull(actual);
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        final var listOfPayrollRuns = (List<PayrollRun>) actual.getBody().getDetails();
        assertEquals(1, listOfPayrollRuns.size());
        assertEquals(payrollRun, listOfPayrollRuns.get(0));
    }

    @Test
    void getPayrollPeriodStartAfterPeriodEndShouldReturnBadRequestResponse() {
        //given
        String employeeId = "EMP123";
        LocalDate periodStart = LocalDate.of(2025, 1, 31);
        LocalDate periodEnd = LocalDate.of(2025, 1, 1);

        //when
        ResponseEntity<ApiResponse> actual = payrollCalculationController.getPayroll(employeeId, periodStart, periodEnd);

        //then
        assertNotNull(actual);
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        assertEquals("INVALID_DATE_RANGE", actual.getBody().getCode());
        assertEquals("periodEnd cannot be before periodStart", actual.getBody().getMessage());
    }

    @Test
    void getPayrollWithPeriodStartAndNoPeriodEndParamShouldReturnBadRequestResponse() {
        //given
        LocalDate periodStart = LocalDate.of(2025, 1, 31);

        //when
        ResponseEntity<ApiResponse> actual = payrollCalculationController.getPayroll(null, periodStart, null);

        //then
        assertNotNull(actual);
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        assertEquals("MISSING_PARAMETER", actual.getBody().getCode());
        assertEquals("periodStart is required when periodEnd is provided",
                actual.getBody().getMessage());
    }

    @Test
    void getPayrollWithNoQueryParamShouldReturnBadRequestResponse() {
        //when
        ResponseEntity<ApiResponse> actual = payrollCalculationController.getPayroll(null, null, null);

        //then
        assertNotNull(actual);
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        assertEquals("INVALID_REQUEST", actual.getBody().getCode());
        assertEquals("At least one parameter (employeeId, periodStart, periodEnd) must be provided",
                actual.getBody().getMessage());
    }

    private PayrollRunResponse getPayrollRunResponse(final String employeeId,
                                                     final LocalDate periodStart,
                                                     final LocalDate periodEnd) {
        return PayrollRunResponse.builder().employeeId(employeeId)
                .payPeriodStart(periodStart).payPeriodEnd(periodEnd)
                .netPay(valueOf(5000.00))
                .grossPay(valueOf(6000.00))
                .taxAmount(valueOf(100.00))
                .holidayPay(valueOf(10.00))
                .payGroupId(1001)
                .proratedAmount(valueOf(2.00))
                .benefitsAmount(valueOf(300.00))
                .deductionsAmount(valueOf(200.00)).build();
    }
}
