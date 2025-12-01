package com.tw.coupang.one_payroll.payslip.service;

import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.EmployeeMaster.Exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.EmployeeMaster.Repository.EmployeeMasterRepository;
import com.tw.coupang.one_payroll.payslip.dto.MonthlyPayslipSummaryDto;
import com.tw.coupang.one_payroll.payslip.dto.YtdSummaryForPdfDto;
import com.tw.coupang.one_payroll.payslip.dto.YtdSummaryResponse;
import com.tw.coupang.one_payroll.payslip.entity.Payslip;
import com.tw.coupang.one_payroll.payslip.repository.PayslipRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class YtdSummaryServiceImpl implements YtdSummaryService
{

    private final PayslipRepository payslipRepository;
    private final EmployeeMasterRepository employeeMasterRepository;

    public YtdSummaryServiceImpl(PayslipRepository payslipRepository,
                                 EmployeeMasterRepository employeeMasterRepository)
    {
        this.payslipRepository = payslipRepository;
        this.employeeMasterRepository = employeeMasterRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public YtdSummaryForPdfDto getYtdSummaryWithBreakdown(String employeeId, int year)
    {
        log.info("Calculating YTD with monthly breakdown for employee: {}, year: {}", employeeId, year);

        EmployeeMaster employee = employeeMasterRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with ID '" + employeeId + "' not found"));

        if(employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new EmployeeNotFoundException("Employee with ID '" + employeeId + "' is not active");
        }

        List<Payslip> payslips = payslipRepository.findByEmployeeIdAndYear(employeeId, year);

        if (payslips.isEmpty()) {
            log.info("No payslips found for employee: {} in year: {}", employeeId, year);
            return new YtdSummaryForPdfDto(
                    employee.getEmployeeId(),
                    employee.getFirstName() + " " + employee.getLastName(),
                    employee.getDepartment(),
                    employee.getDesignation(),
                    year,
                    Map.of(),
                    YtdSummaryResponse.zero()
            );
        }

        log.info("Total number of payslips found: {}", payslips.size());

        YtdSummaryResponse ytdTotals = calculateYtdTotals(payslips);

        Map<String, MonthlyPayslipSummaryDto> monthlyBreakdown = buildMonthlySummaryMap(payslips);

        return new YtdSummaryForPdfDto(
                employee.getEmployeeId(),
                employee.getFirstName() + " " + employee.getLastName(),
                employee.getDepartment(),
                employee.getDesignation(),
                year,
                monthlyBreakdown,
                ytdTotals);

    }

    @Override
    @Transactional(readOnly = true)
    public YtdSummaryResponse getYtdSummaryDetails(String employeeId, int year)
    {
        log.info("Fetching YTD summary for employee: {}, year: {}", employeeId, year);

        employeeMasterRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with ID '" + employeeId + "' not found"));
        List<Payslip> payslips = payslipRepository.findByEmployeeIdAndYear(employeeId, year);

        log.info("Number of payslips fetched: {}", payslips.size());

        if (payslips.isEmpty())
            return YtdSummaryResponse.zero();
        else
            return calculateYtdTotals(payslips);
    }

    private Map<String, MonthlyPayslipSummaryDto> buildMonthlySummaryMap(List<Payslip> payslips) {
        return payslips.stream()
                .map(this::toMonthlyPayslipSummaryDto)
                .collect(Collectors.toMap(
                        MonthlyPayslipSummaryDto::monthName,
                        Function.identity()
                ));
    }

    private MonthlyPayslipSummaryDto toMonthlyPayslipSummaryDto(Payslip payslip)
    {
        int monthNumber = payslip.getPayPeriod().getMonthValue();
        String monthName = payslip.getPayPeriod().getMonth().name();
        int year = payslip.getPayPeriod().getYear();
        BigDecimal totalDeductions = payslip.getDeductions().values().stream()
                                     .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlyPayslipSummaryDto(monthName,
                monthNumber,
                year,
                payslip.getGrossPay(),
                payslip.getNetPay(),
                payslip.getBenefits(),
                totalDeductions);
    }

    private YtdSummaryResponse calculateYtdTotals(List<Payslip> monthlyPayslips)
    {
        log.info("Calculating YTD totals from monthly breakdown with {} months", monthlyPayslips.size());

        BigDecimal totalGross = monthlyPayslips.stream()
                .map(Payslip::getGrossPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNet = monthlyPayslips.stream()
                .map(Payslip::getNetPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDeductions = monthlyPayslips.stream()
                .flatMap(p -> p.getDeductions().values().stream())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBenefit = monthlyPayslips.stream()
                .map(Payslip::getBenefits)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        log.info("YTD Summary calculated - Gross: {}, Net: {}, Tax: {}, Benefit: {}",
                totalGross, totalNet, totalDeductions, totalBenefit);

        return new YtdSummaryResponse(
                totalGross,
                totalNet,
                totalDeductions,
                totalBenefit);
    }
}
