package com.tw.coupang.one_payroll.paygroup.controller;

import com.tw.coupang.one_payroll.paygroups.controller.PayGroupController;
import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupCreateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupResponse;
import com.tw.coupang.one_payroll.paygroups.service.PayGroupService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayGroupControllerTest {

    @InjectMocks
    private PayGroupController payGroupController;

    @Mock
    private PayGroupService payGroupService;

    @Test
    void createPayGroup_nullResponse() {
        PayGroupCreateRequest request = new PayGroupCreateRequest();
        request.setGroupName("Biweekly Engineers");

        when(payGroupService.createPayGroup(any(PayGroupCreateRequest.class)))
                .thenReturn(null);

        ResponseEntity<PayGroupResponse> response =
                payGroupController.create(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }
}