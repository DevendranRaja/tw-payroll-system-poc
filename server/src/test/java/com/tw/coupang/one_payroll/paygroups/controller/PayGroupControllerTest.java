package com.tw.coupang.one_payroll.paygroups.controller;

import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupCreateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupResponse;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import com.tw.coupang.one_payroll.paygroups.exception.DuplicatePayGroupException;
import com.tw.coupang.one_payroll.paygroups.service.PayGroupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayGroupControllerTest {

    @InjectMocks
    private PayGroupController payGroupController;

    @Mock
    private PayGroupService payGroupService;

    @Test
    void createPayGroup_duplicatePayGroup_throwsException() {
        PayGroupCreateRequest request = validRequest();

        when(payGroupService.create(any(PayGroupCreateRequest.class)))
                .thenThrow(new DuplicatePayGroupException("Pay group already exists"));

        DuplicatePayGroupException ex = assertThrows(
                DuplicatePayGroupException.class,
                () -> payGroupController.createPayGroup(request)
        );

        assertEquals("Pay group already exists", ex.getMessage());
        verify(payGroupService, times(1)).create(request);
    }

    @Test
    void createPayGroup_success() {
        PayGroupCreateRequest request = validRequest();

        PayGroupResponse expected = PayGroupResponse.builder()
                .payGroupId(1)
                .build();

        when(payGroupService.create(any(PayGroupCreateRequest.class)))
                .thenReturn(expected);

        ResponseEntity<PayGroupResponse> response =
                payGroupController.createPayGroup(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expected.getPayGroupId(), response.getBody().getPayGroupId());

        verify(payGroupService, times(1)).create(request);
    }

    private PayGroupCreateRequest validRequest() {
        return PayGroupCreateRequest.builder()
                .groupName("Monthly Engineers")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.valueOf(10))
                .benefitRate(BigDecimal.valueOf(5))
                .deductionRate(BigDecimal.valueOf(3))
                .build();
    }
}