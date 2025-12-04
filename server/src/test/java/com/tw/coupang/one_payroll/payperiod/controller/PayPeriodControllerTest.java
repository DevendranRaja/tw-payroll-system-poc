package com.tw.coupang.one_payroll.payperiod.controller;

import com.tw.coupang.one_payroll.payperiod.dto.request.PayPeriod;
import com.tw.coupang.one_payroll.payperiod.dto.request.PayPeriodCreateRequest;
import com.tw.coupang.one_payroll.payperiod.dto.response.PayPeriodResponse;
import com.tw.coupang.one_payroll.payperiod.exception.OverlappingPayPeriodException;
import com.tw.coupang.one_payroll.payperiod.service.PayPeriodService;
import com.tw.coupang.one_payroll.payroll.dto.response.ApiResponse;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayPeriodControllerTest {

    @InjectMocks
    private PayPeriodController payPeriodController;

    @Mock
    private PayPeriodService payPeriodService;

    @Test
    void createPayPeriodWithNullRequestShouldThrowMethodArgumentNotValid() {
        PayPeriodCreateRequest request = null;

        assertThrows(NullPointerException.class, () -> payPeriodController.createPayPeriod(request));
    }

    @Test
    void createPayPeriodWhenServiceThrowsOverlapExceptionShouldReturnConflict() {
        PayPeriod payPeriod = PayPeriod.builder()
                .startDate(LocalDate.of(2025, 10, 1))
                .endDate(LocalDate.of(2025, 10, 31))
                .build();

        PayPeriodCreateRequest request = PayPeriodCreateRequest.builder()
                .payGroupId(1)
                .payPeriod(payPeriod)
                .build();

        when(payPeriodService.create(request))
                .thenThrow(new OverlappingPayPeriodException("Overlap detected"));

        OverlappingPayPeriodException ex = assertThrows(
                OverlappingPayPeriodException.class,
                () -> payPeriodController.createPayPeriod(request)
        );

        assertEquals("Overlap detected", ex.getMessage());
        verify(payPeriodService, times(1)).create(request);
    }

    @Test
    void createPayPeriodWithValidRequestShouldReturnCreated() {
        PayPeriod payPeriod = PayPeriod.builder()
                .startDate(LocalDate.of(2025, 10, 1))
                .endDate(LocalDate.of(2025, 10, 31))
                .build();

        PayPeriodCreateRequest request = PayPeriodCreateRequest.builder()
                .payGroupId(1)
                .payPeriod(payPeriod)
                .build();

        PayPeriodResponse response = PayPeriodResponse.builder()
                .id(100)
                .build();

        when(payPeriodService.create(request)).thenReturn(response);

        ResponseEntity<ApiResponse> actual = payPeriodController.createPayPeriod(request);

        assertNotNull(actual);
        assertEquals(HttpStatus.CREATED, actual.getStatusCode());
        assertNotNull(actual.getBody());
        assertEquals("PAY_PERIOD_CREATED", actual.getBody().getCode());
        assertEquals(100, ((PayPeriodResponse) actual.getBody().getDetails()).id());

        verify(payPeriodService, times(1)).create(request);
    }
}
