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
        EmployeeMaster employee = employeeMasterService.getEmployeeById(request.getEmployeeId());

        if(employee.getStatus() != EmployeeStatus.ACTIVE)
            throw new EmployeeInactiveException("Employee with ID '" + request.getEmployeeId() + "' is not active");

        PayGroup payGroup = payGroupValidator.validatePayGroupExists(employee.getPayGroupId());

        log.info("Payroll calculation request received for Employee ID: {}, Pay Group: {}", request.getEmployeeId(), payGroup.getGroupName());

        payrollCalculationValidator.validatePayPeriodAgainstPayGroup(request.getPeriodStart(), request.getPeriodEnd(), payGroup);

        // TODO: Make a call to Payroll Calculation Engine here and get the response

        return ApiResponse.success("PAYROLL_CALCULATION_SUCCESS", "Payroll calculation completed successfully", null);
    }
}
