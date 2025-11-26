package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.EmployeeMaster.Exception.EmployeeInactiveException;
import com.tw.coupang.one_payroll.EmployeeMaster.Service.EmployeeMasterService;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.validator.PayGroupValidator;
import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.dto.response.ApiResponse;
import com.tw.coupang.one_payroll.payroll.validator.PayrollCalculationValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PayrollCalculationServiceImpl implements PayrollCalculationService {

    private final EmployeeMasterService employeeMasterService;
    private final PayGroupValidator payGroupValidator;
    private final PayrollCalculationValidator payrollCalculationValidator;

    public PayrollCalculationServiceImpl(EmployeeMasterService employeeMasterService, PayGroupValidator payGroupValidator, PayrollCalculationValidator payrollCalculationValidator) {
        this.employeeMasterService = employeeMasterService;
        this.payGroupValidator = payGroupValidator;
        this.payrollCalculationValidator = payrollCalculationValidator;
    }

    @Override
    public ApiResponse calculate(PayrollCalculationRequest request) {
        log.info("Initiating payroll calculation for employeeId={}", request.getEmployeeId());

        EmployeeMaster employee = employeeMasterService.getEmployeeById(request.getEmployeeId());

        if(employee.getStatus() != EmployeeStatus.ACTIVE) {
            log.warn("Inactive employee attempted payroll calculation. employeeId={}", request.getEmployeeId());
            throw new EmployeeInactiveException("Employee with ID '" + request.getEmployeeId() + "' is not active");
        }

        PayGroup payGroup = payGroupValidator.validatePayGroupExists(employee.getPayGroupId());

        log.info("Validated employee and pay group for employeeId={}, payGroupId={}", request.getEmployeeId(), employee.getPayGroupId());

        payrollCalculationValidator.validatePayPeriodAgainstPayGroup(request.getPeriodStart(), request.getPeriodEnd(), payGroup);

        log.info("Pay period validated for employeeId={} ({} → {})", request.getEmployeeId(), request.getPeriodStart(), request.getPeriodEnd());

        // TODO: Make a call to Payroll Calculation Engine here and get the response
        log.debug("Calling Payroll Calculation Engine for employeeId={}", request.getEmployeeId());

        return ApiResponse.success("PAYROLL_CALCULATION_SUCCESS", "Payroll calculation completed successfully", null);
    }
}
