package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.employee_master.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.employee_master.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeInactiveException;
import com.tw.coupang.one_payroll.employee_master.Service.EmployeeMasterService;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.validator.PayGroupValidator;
import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.dto.response.ApiResponse;
import com.tw.coupang.one_payroll.payroll.validator.PayrollCalculationValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
        final String employeeId = request.getEmployeeId();
        log.info("Initiating payroll calculation for employeeId={}", employeeId);

        EmployeeMaster employee = employeeMasterService.getEmployeeById(employeeId);

        if(employee.getStatus() != EmployeeStatus.ACTIVE) {
            log.warn("Inactive employee attempted payroll calculation. employeeId={}", employeeId);
            throw new EmployeeInactiveException("Employee with ID '" + employeeId + "' is not active");
        }

        final Integer payGroupId = employee.getPayGroupId();

        PayGroup payGroup = payGroupValidator.validatePayGroupExists(payGroupId);
        final LocalDate startDate = request.getPayPeriod().getStartDate();
        final LocalDate endDate = request.getPayPeriod().getEndDate();

        log.info("Validated employee and pay group for employeeId={}, payGroupId={}", employeeId, payGroupId);

        payrollCalculationValidator.validatePayPeriodAgainstPayGroup(startDate, endDate, payGroup);

        log.info("Pay period validated for employeeId={} ({} → {})", employeeId, startDate, endDate);

        // TODO (CP-16): Make a call to Payroll Calculation Engine here and get the response
        log.debug("Calling Payroll Calculation Engine for employeeId={}", employeeId);

        return ApiResponse.success("PAYROLL_CALCULATION_SUCCESS", "Payroll calculation completed successfully", null);
    }
}
