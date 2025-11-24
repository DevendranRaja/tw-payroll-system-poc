package com.tw.coupang.one_payroll.paygroups.controller;

import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupCreateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupUpdateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupDetailsResponse;
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
import java.util.Collections;
import java.util.List;

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
    void createPayGroupWhenDuplicatePayGroupThenThrowsException() {
        PayGroupCreateRequest request = buildCreateRequest();

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
    void createPayGroupIsSuccess() {
        PayGroupCreateRequest request = buildCreateRequest();

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
    void updatePayGroupIsSuccess() {
        PayGroupUpdateRequest request = buildUpdateRequest();

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
    void updatePayGroupWhenFailureThenThrowsRuntimeException() {
        PayGroupUpdateRequest request = buildUpdateRequest();

        when(payGroupService.update(99, request))
                .thenThrow(new RuntimeException("Update failed"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> payGroupController.updatePayGroup(99, request)
        );

        assertEquals("Update failed", ex.getMessage());
        verify(payGroupService, times(1)).update(99, request);
    }

    @Test
    void getAllPayGroupsWhenNoFilterThenReturnsAll() {
        PayGroupDetailsResponse responseItem = PayGroupDetailsResponse.builder()
                .payGroupId(1)
                .groupName("Monthly Engineers")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.valueOf(10))
                .benefitRate(BigDecimal.valueOf(5))
                .deductionRate(BigDecimal.valueOf(3))
                .build();

        when(payGroupService.getAll(null))
                .thenReturn(List.of(responseItem));

        ResponseEntity<List<PayGroupDetailsResponse>> response =
                payGroupController.getAllPayGroups(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(responseItem.getPayGroupId(), response.getBody().get(0).getPayGroupId());

        verify(payGroupService, times(1)).getAll(null);
    }

    @Test
    void getAllPayGroupsWithPaymentCycleFilterThenReturnsFiltered() {
        PayGroupDetailsResponse responseItem = PayGroupDetailsResponse.builder()
                .payGroupId(2)
                .groupName("Weekly Engineers")
                .paymentCycle(PaymentCycle.WEEKLY)
                .baseTaxRate(BigDecimal.valueOf(8))
                .benefitRate(BigDecimal.valueOf(4))
                .deductionRate(BigDecimal.valueOf(2))
                .build();

        when(payGroupService.getAll(PaymentCycle.WEEKLY))
                .thenReturn(List.of(responseItem));

        ResponseEntity<List<PayGroupDetailsResponse>> response =
                payGroupController.getAllPayGroups(PaymentCycle.WEEKLY);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(responseItem.getPayGroupId(), response.getBody().get(0).getPayGroupId());

        verify(payGroupService, times(1)).getAll(PaymentCycle.WEEKLY);
    }

    @Test
    void getAllPayGroupsWhenEmptyListThenReturnsEmpty() {
        when(payGroupService.getAll(null))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<PayGroupDetailsResponse>> response =
                payGroupController.getAllPayGroups(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());

        verify(payGroupService, times(1)).getAll(null);
    }

    private PayGroupCreateRequest buildCreateRequest() {
        return PayGroupCreateRequest.builder()
                .groupName("Monthly Engineers")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.valueOf(10))
                .benefitRate(BigDecimal.valueOf(5))
                .deductionRate(BigDecimal.valueOf(3))
                .build();
    }

    private PayGroupUpdateRequest buildUpdateRequest() {
        return PayGroupUpdateRequest.builder()
                .groupName("Monthly Engineers Updated")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.valueOf(12))
                .benefitRate(BigDecimal.valueOf(6))
                .deductionRate(BigDecimal.valueOf(4))
                .build();
    }
}