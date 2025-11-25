package com.tw.coupang.one_payroll.payroll.controller;

import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.dto.response.ApiResponse;
import com.tw.coupang.one_payroll.payroll.service.PayrollCalculationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payroll")
public class PayrollCalculationController {

    private final PayrollCalculationService payrollCalculationService;

    public PayrollCalculationController(PayrollCalculationService payrollCalculationService) {
        this.payrollCalculationService = payrollCalculationService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse> calculatePayroll(@Valid @RequestBody PayrollCalculationRequest request) {
        ApiResponse response = payrollCalculationService.calculate(request);
        return ResponseEntity.ok(response);
    }
}
