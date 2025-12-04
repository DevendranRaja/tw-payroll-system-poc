package com.tw.coupang.one_payroll.payroll.service;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.tw.coupang.one_payroll.common.utils.MathsUtils.percentOf;
import static com.tw.coupang.one_payroll.payroll.enums.PayrollStatus.PROCESSED;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class PayrollCalculationServiceImpl implements PayrollCalculationService {

    private static final BigDecimal PF_RATE = BigDecimal.valueOf(0.12); // 12%
    private static final BigDecimal PROFESSIONAL_TAX_RATE = BigDecimal.valueOf(200); // flat
    private static final String INCOME_TAX = "Income Tax";
    private static final String PROVIDENT_FUND = "Provident Fund";
    private static final String PROFESSIONAL_TAX = "Professional Tax";
    private static final String BASIC_SALARY = "Basic Salary";
    private static final String HRA = "HRA";
    private static final String BONUS = "Bonus";

    private final EmployeeMasterService employeeMasterService;
    private final PayGroupValidator payGroupValidator;
    private final PayrollRunRepository payrollRunRepository;
    private final PayPeriodCycleValidator payPeriodCycleValidator;
    private final EarningTypeRepository earningTypeRepository;
    private final PayrollEarningsRepository payrollEarningsRepository;
    private final DeductionTypeRepository deductionTypeRepository;
    private final PayrollDeductionsRepository payrollDeductionsRepository;

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

        //TODO: Refactor salary calculation for different pay cycles
        final var monthlySalary = employee.getBaseSalary().multiply(BigDecimal.valueOf(30)); // assuming 30 days in a month

        final Map<String, BigDecimal> earningsMap = buildEarningMap(monthlySalary);
        BigDecimal grossPay = earningsMap.values().stream()
                .reduce(ZERO, BigDecimal::add).setScale(2, HALF_UP);
        final Map<String, BigDecimal> deductionsMap = buildDeductionMap(
                earningsMap.get(BASIC_SALARY), grossPay, payGroup);

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
        if (grossPay.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("Gross pay must be greater than zero to calculate net pay");
        }

        final var otherDeductions = percentOf(payGroup.getDeductionRate(), grossPay);
        BigDecimal totalDeductions = deductionsMap.values().stream()
                .reduce(ZERO, BigDecimal::add).add(otherDeductions).setScale(2, HALF_UP);
        if (totalDeductions.doubleValue() >= grossPay.doubleValue()) {
            throw new IllegalStateException("Total deductions exceed or equal gross pay, cannot compute net pay");
        }

        final var benefits = percentOf(payGroup.getBenefitRate(), grossPay);

        final var netPay = grossPay.subtract(totalDeductions).add(benefits).setScale(2, HALF_UP);

        return PayrollRun.builder().employeeId(request.getEmployeeId())
                .payPeriodStart(request.getPayPeriod().getStartDate())
                .payPeriodEnd(request.getPayPeriod().getEndDate())
                .grossPay(grossPay)
                .netPay(netPay)
                .taxDeduction(deductionsMap.get(INCOME_TAX))
                .benefitAddition(benefits)
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

    private Map<String, BigDecimal> buildEarningMap(final BigDecimal monthlySalary) {

        final Map<String, BigDecimal> map = new HashMap<>();

        final var basic = percentOf(monthlySalary, BigDecimal.valueOf(40));
        final var hra = percentOf(basic, BigDecimal.valueOf(50));
        final var bonus = percentOf(basic, BigDecimal.valueOf(5)); // bonus part can be refactored for variable bonus

        map.put(BASIC_SALARY, basic);
        map.put(HRA, hra);
        map.put(BONUS, bonus);
        return map;
    }

    private Map<String, BigDecimal> buildDeductionMap(final BigDecimal basic,
                                                      final BigDecimal gross,
                                                      final PayGroup payGroup) {
        Map<String, BigDecimal> map = new HashMap<>();

        final var tax = percentOf(payGroup.getBaseTaxRate(), gross);
        final var providentFund = basic.multiply(PF_RATE).setScale(2, HALF_UP);
        final var professionalTax = PROFESSIONAL_TAX_RATE.setScale(2, HALF_UP);

        map.put(INCOME_TAX, tax);
        map.put(PROVIDENT_FUND, providentFund);
        map.put(PROFESSIONAL_TAX, professionalTax);
        return map;
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
