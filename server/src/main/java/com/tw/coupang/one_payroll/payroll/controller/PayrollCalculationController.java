package com.tw.coupang.one_payroll.payroll.controller;

import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.dto.response.ApiResponse;
import com.tw.coupang.one_payroll.payroll.service.PayrollCalculationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/payroll")
public class PayrollCalculationController {

    private final PayrollCalculationService payrollCalculationService;

    public PayrollCalculationController(PayrollCalculationService payrollCalculationService) {
        this.payrollCalculationService = payrollCalculationService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse> calculatePayroll(@Valid @RequestBody PayrollCalculationRequest request) {
        log.info("Received payroll calculation request for employeeId={}", request.getEmployeeId());

        ApiResponse response = payrollCalculationService.calculate(request);

        log.info("Successfully calculated payroll for employeeId={}", request.getEmployeeId());
        
        return ResponseEntity.ok(response);
    }
}
