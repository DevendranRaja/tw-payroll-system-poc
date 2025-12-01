package com.tw.coupang.one_payroll.payroll.controller;

import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.dto.response.ApiResponse;
import com.tw.coupang.one_payroll.payroll.dto.response.PayrollRunResponse;
import com.tw.coupang.one_payroll.payroll.service.PayrollCalculationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
        final var payrollResponse = payrollCalculationService.calculate(request);
        return ResponseEntity.ok(ApiResponse.success(
                "PAYROLL_CALCULATION_SUCCESS", "Payroll calculation completed successfully", payrollResponse));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getPayroll(@RequestParam(required = false) final String employeeId,
                                                  @RequestParam(required = false) final LocalDate periodStart,
                                                  @RequestParam(required = false) final LocalDate periodEnd) {
        if (isNull(employeeId) && isNull(periodStart) && isNull(periodEnd)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_REQUEST",
                    "At least one parameter (employeeId, periodStart, periodEnd) must be provided"));
        }
        if ((isNull(periodStart) && nonNull(periodEnd)) || (nonNull(periodStart) && isNull(periodEnd))) {
            return ResponseEntity.badRequest().body(ApiResponse.error("MISSING_PARAMETER",
                    "periodStart is required when periodEnd is provided"));
        }
        if (nonNull(periodStart) && nonNull(periodEnd) && periodEnd.isBefore(periodStart)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_DATE_RANGE",
                    "periodEnd cannot be before periodStart"));
        }
        List<PayrollRunResponse> payrollRuns = payrollCalculationService.getPayroll(employeeId, periodStart, periodEnd);
        return ResponseEntity.ok(ApiResponse.success(
                "PAYROLL_FETCH_SUCCESS", "Payroll records fetched successfully", payrollRuns));
    }
}
