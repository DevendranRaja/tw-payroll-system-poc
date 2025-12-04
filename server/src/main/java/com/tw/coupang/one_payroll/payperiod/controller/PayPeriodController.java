package com.tw.coupang.one_payroll.payperiod.controller;

import com.tw.coupang.one_payroll.payperiod.dto.request.PayPeriodCreateRequest;
import com.tw.coupang.one_payroll.payperiod.dto.response.PayPeriodResponse;
import com.tw.coupang.one_payroll.payperiod.service.PayPeriodService;
import com.tw.coupang.one_payroll.payroll.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/pay-periods")
public class PayPeriodController {

    private final PayPeriodService payPeriodService;

    public PayPeriodController(PayPeriodService payPeriodService) {
        this.payPeriodService = payPeriodService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createPayPeriod(@Valid @RequestBody PayPeriodCreateRequest request) {
        log.info("Received pay period request for payGroupId={}", request.getPayGroupId());

        PayPeriodResponse response = payPeriodService.create(request);

        log.info("Pay period created successfully with id={}", response.id());

        return ResponseEntity.status(201)
                .body(
                        ApiResponse.success("PAY_PERIOD_CREATED", "Pay period created successfully", response)
                );
    }
}
