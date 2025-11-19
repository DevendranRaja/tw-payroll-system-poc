package com.tw.coupang.one_payroll.paygroups.controller;

import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupCreateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupUpdateRequest;
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

    @Test
    void updatePayGroup_success() {
        PayGroupUpdateRequest request = validUpdateRequest();

        PayGroupResponse expected = PayGroupResponse.builder()
                .payGroupId(1)
                .build();

        when(payGroupService.update(1, request)).thenReturn(expected);

        ResponseEntity<PayGroupResponse> response =
                payGroupController.updatePayGroup(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expected.getPayGroupId(), response.getBody().getPayGroupId());

        verify(payGroupService, times(1)).update(1, request);
    }

    @Test
    void updatePayGroup_failure_throwsRuntimeException() {
        PayGroupUpdateRequest request = validUpdateRequest();

        // Simulate failure when updating
        when(payGroupService.update(99, request))
                .thenThrow(new RuntimeException("Update failed"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> payGroupController.updatePayGroup(99, request)
        );

        assertEquals("Update failed", ex.getMessage());
        verify(payGroupService, times(1)).update(99, request);
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

    private PayGroupUpdateRequest validUpdateRequest() {
        return PayGroupUpdateRequest.builder()
                .groupName("Monthly Engineers Updated")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.valueOf(12))
                .benefitRate(BigDecimal.valueOf(6))
                .deductionRate(BigDecimal.valueOf(4))
                .build();
    }
}