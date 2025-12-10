package com.tw.coupang.one_payroll.payroll.service.impl;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.employee_master.enums.EmployeeStatus;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeInactiveException;
import com.tw.coupang.one_payroll.employee_master.service.EmployeeMasterService;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.validator.PayGroupValidator;
import com.tw.coupang.one_payroll.payperiod.validator.PayPeriodCycleValidator;
import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.dto.response.PayrollRunResponse;
import com.tw.coupang.one_payroll.payroll.entity.PayrollDeductions;
import com.tw.coupang.one_payroll.payroll.entity.PayrollEarnings;
import com.tw.coupang.one_payroll.payroll.entity.PayrollRun;
import com.tw.coupang.one_payroll.payroll.repository.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.BASIC_SALARY;
import static com.tw.coupang.one_payroll.payroll.enums.PayrollComponentType.INCOME_TAX;
import static com.tw.coupang.one_payroll.payroll.enums.PayrollStatus.PROCESSED;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class PayrollCalculationServiceImpl implements PayrollCalculationService {

    private final EmployeeMasterService employeeMasterService;
    private final PayGroupValidator payGroupValidator;
    private final PayrollRunRepository payrollRunRepository;
    private final PayPeriodCycleValidator payPeriodCycleValidator;
    private final EarningTypeRepository earningTypeRepository;
    private final PayrollEarningsRepository payrollEarningsRepository;
    private final DeductionTypeRepository deductionTypeRepository;
    private final PayrollDeductionsRepository payrollDeductionsRepository;
    private final EarningsStrategyRegistry earningsRegistry;
    private final DeductionStrategyRegistry deductionRegistry;
    private final GrossToNetPipelineProcessor grossToNetPipelineProcessor;

    @Override
    public PayrollRunResponse calculate(PayrollCalculationRequest request) {
        final String employeeId = request.getEmployeeId();
        log.info("Initiating payroll calculation for employeeId={}", employeeId);

        EmployeeMaster employee = employeeMasterService.getEmployeeById(employeeId);

        if(employee.getStatus() != EmployeeStatus.ACTIVE) {
            log.warn("Inactive employee attempted payroll calculation. employeeId={}", employeeId);
            throw new EmployeeInactiveException("Employee with ID '" + employeeId + "' is not active");
        }

        final Integer payGroupId = employee.getPayGroupId();
        final var payGroup = payGroupValidator.validatePayGroupExists(payGroupId);
        final LocalDate startDate = request.getPayPeriod().getStartDate();
        final LocalDate endDate = request.getPayPeriod().getEndDate();

        log.info("Validated employee and pay group for employeeId={}, payGroupId={}", employeeId, payGroupId);

        payPeriodCycleValidator.validatePayPeriodAgainstPayGroup(startDate, endDate, payGroup);

        log.info("Pay period validated for employeeId={} ({} → {})", employeeId, startDate, endDate);

        if (employee.getBaseSalary().compareTo(ZERO) <= 0) {
            log.warn("Employee with non-positive base salary attempted payroll calculation. employeeId={}", employeeId);
            throw new IllegalArgumentException("Base salary must be greater than zero for payroll calculation");
        }

        final var numberOfDays = endDate.toEpochDay() - startDate.toEpochDay() + 1;
        //TODO: Refactor salary calculation for different pay cycles
        final var monthlySalary = employee.getBaseSalary().multiply(BigDecimal.valueOf(numberOfDays));

        // Basic Salary Calculation
        final var basicSalary = earningsRegistry.getAll().stream()
                .filter(s -> s.getCode().equals(BASIC_SALARY.getValue()))
                .findFirst()
                .orElseThrow()
                .calculate(monthlySalary, ZERO);

        //Earnings Calculation
        final var earningsMap =
                earningsRegistry.getAll().stream()
                        .collect(Collectors.toMap(
                                EarningComponentStrategy::getCode,
                                s -> s.calculate(monthlySalary, basicSalary)
                        ));

        BigDecimal grossPay = earningsMap.values().stream()
                .reduce(ZERO, BigDecimal::add).setScale(2, HALF_UP);

        //Deductions Calculation
        Map<String, BigDecimal> deductionsMap =
                deductionRegistry.getAll().stream()
                        .collect(Collectors.toMap(
                                DeductionComponentStrategy::getCode,
                                s -> s.calculate(grossPay, basicSalary, payGroup)
                        ));

        final var payrollRun = payrollGrossToNetPayCalculation(grossPay, deductionsMap, payGroup, request);

        payrollRunRepository.save(payrollRun);

        // -------- Persist Earnings --------
        persistEarnings(earningsMap, payrollRun);

        // -------- Persist Deductions --------
        persistDeductions(deductionsMap, payrollRun);

        log.info("Payroll calculation completed for Employee ID: {}, Pay Period: {} to {}",
                request.getEmployeeId(), request.getPayPeriod().getStartDate(), request.getPayPeriod().getEndDate());

        return PayrollRunResponse.builder()
                .employeeId(payrollRun.getEmployeeId())
                .payGroupId(payGroup.getId())
                .payPeriodStart(payrollRun.getPayPeriodStart())
                .payPeriodEnd(payrollRun.getPayPeriodEnd())
                .grossPay(payrollRun.getGrossPay())
                .netPay(payrollRun.getNetPay())
                .benefitsAmount(payrollRun.getBenefitAddition())
                .taxAmount(payrollRun.getTaxDeduction())
                .build();
    }

    public PayrollRun payrollGrossToNetPayCalculation(final BigDecimal grossPay,
                                                      final Map<String, BigDecimal> deductionsMap,
                                                      final PayGroup payGroup,
                                                      final PayrollCalculationRequest request) {
        PayrollContext contextInit = new PayrollContext(grossPay, ZERO, ZERO, ZERO, deductionsMap, payGroup);

        final var context = grossToNetPipelineProcessor.process(contextInit);

        return PayrollRun.builder().employeeId(request.getEmployeeId())
                .payPeriodStart(request.getPayPeriod().getStartDate())
                .payPeriodEnd(request.getPayPeriod().getEndDate())
                .grossPay(grossPay)
                .netPay(context.getNetPay())
                .taxDeduction(deductionsMap.get(INCOME_TAX.getValue()))
                .benefitAddition(context.getBenefits())
                .status(PROCESSED)
                .build();
    }

    private void persistEarnings(final Map<String, BigDecimal> earningMap,
                                 final PayrollRun payrollRun) {
        List<PayrollEarnings> earningsList = earningTypeRepository.findAll()
                .stream()
                .map(type -> {

                    BigDecimal amount = earningMap.getOrDefault(type.getName(), ZERO);

                    if (amount.compareTo(ZERO) <= 0) {
                        return null;
                    }

                    PayrollEarnings payrollEarnings = new PayrollEarnings();
                    payrollEarnings.setPayrollRun(payrollRun);
                    payrollEarnings.setEarningType(type);
                    payrollEarnings.setAmount(amount);

                    return payrollEarnings;
                })
                .filter(Objects::nonNull)
                .toList();

        if (!earningsList.isEmpty()) {
            payrollEarningsRepository.saveAll(earningsList);
        }
    }

    private void persistDeductions(final Map<String, BigDecimal> deductionMap,
                                   final PayrollRun payrollRun) {
        List<PayrollDeductions> deductionList = deductionTypeRepository.findAll()
                .stream()
                .map(type -> {

                    BigDecimal amount = deductionMap.getOrDefault(type.getName(), ZERO);

                    if (amount.compareTo(ZERO) <= 0) {
                        return null;
                    }

                    PayrollDeductions payrollDeductions = new PayrollDeductions();
                    payrollDeductions.setPayrollRun(payrollRun);
                    payrollDeductions.setDeductionType(type);
                    payrollDeductions.setAmount(amount);

                    return payrollDeductions;
                })
                .filter(Objects::nonNull)
                .toList();

        if (!deductionList.isEmpty()) {
            payrollDeductionsRepository.saveAll(deductionList);
        }
    }

    @Override
    public List<PayrollRunResponse> getPayroll(final String employeeId,
                                               final LocalDate periodStart,
                                               final LocalDate periodEnd) {
        final var payrollRuns = payrollRunRepository.findByEmployeeIdOrPayPeriodStartAndPayPeriodEnd(
                employeeId, periodStart, periodEnd);
        log.info("Fetched {} payroll records for Employee ID: {}, Period Start: {}, Period End: {}",
                payrollRuns.size(), employeeId, periodStart, periodEnd);
        return payrollRuns.stream().map(payrollRun -> PayrollRunResponse.builder()
                        .employeeId(payrollRun.getEmployeeId()).payPeriodStart(payrollRun.getPayPeriodStart())
                        .payPeriodEnd(payrollRun.getPayPeriodEnd())
                        .netPay(payrollRun.getNetPay()).grossPay(payrollRun.getGrossPay())
                        .benefitsAmount(payrollRun.getBenefitAddition()).taxAmount(payrollRun.getTaxDeduction())
                        .build())
                .toList();
    }

}
