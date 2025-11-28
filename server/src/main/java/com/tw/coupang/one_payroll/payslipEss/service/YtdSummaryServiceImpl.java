package com.tw.coupang.one_payroll.payslipEss.service;

import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.EmployeeMaster.Exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.EmployeeMaster.Repository.EmployeeMasterRepository;
import com.tw.coupang.one_payroll.payslipEss.dto.MonthlyPayslipSummaryDto;
import com.tw.coupang.one_payroll.payslipEss.dto.YtdSummaryForPdfDto;
import com.tw.coupang.one_payroll.payslipEss.dto.YtdSummaryResponse;
import com.tw.coupang.one_payroll.payslipEss.entity.Payslip;
import com.tw.coupang.one_payroll.payslipEss.repository.PayslipRepository;
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

        log.info("Total number of payslips found: {}", payslips.size());

//        if (payslips.isEmpty()) {
//            log.info("No payslips found for employee: {} in year: {}", employeeId, year);
//            return YtdSummaryResponse.zero();
//        }

//        if (payslips.isEmpty()) {
//            log.info("No payslips found for employee: {} in year: {}", employeeId, year);
//            return new YtdSummaryWithBreakdown(
//                    employeeId,
//                    year,
//                    List.of(),
//                    YtdTotals.zero()
//            );
//        }


        Map<String, MonthlyPayslipSummaryDto> monthlyBreakdown = buildMonthlySummaryMap(payslips);

        System.out.println("payslipsByMonth: " + monthlyBreakdown);

        YtdSummaryResponse ytdTotals = calculateYtdTotals(
                monthlyBreakdown.values().stream().toList());

        return new YtdSummaryForPdfDto(
                employeeId,
                "name",
                "department",
                "designation",
                year,
                monthlyBreakdown,
                ytdTotals);

    }

    private Map<String, MonthlyPayslipSummaryDto> buildMonthlySummaryMap(List<Payslip> monthPayslips)
    {
        return monthPayslips.stream()
                .map(this::toMonthlyPayslipSummaryDto)
                .collect(Collectors.toMap(
                        MonthlyPayslipSummaryDto::monthName,
                        Function.identity(),
                        (existing, replacement) -> existing));
    }

    private MonthlyPayslipSummaryDto toMonthlyPayslipSummaryDto(Payslip payslip)
    {
        int monthNumber = payslip.getPayPeriod().getMonthValue();
        String monthName = payslip.getPayPeriod().getMonth().name();
        int year = payslip.getPayPeriod().getYear();

        return new MonthlyPayslipSummaryDto(monthName,
                monthNumber,
                year,
                payslip.getGrossPay(),
                payslip.getNetPay(),
                payslip.getBenefits(),
                payslip.getTax());
    }

    private YtdSummaryResponse calculateYtdTotals(List<MonthlyPayslipSummaryDto> monthlyBreakdown)
    {
        log.info("Calculating YTD totals from monthly breakdown with {} months", monthlyBreakdown.size());

        BigDecimal totalGross = monthlyBreakdown.stream()
                .map(MonthlyPayslipSummaryDto::grossPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNet = monthlyBreakdown.stream()
                .map(MonthlyPayslipSummaryDto::netPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTax = monthlyBreakdown.stream()
                .map(MonthlyPayslipSummaryDto::deductions)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBenefit = monthlyBreakdown.stream()
                .map(MonthlyPayslipSummaryDto::benefit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        log.info("YTD Summary calculated - Gross: {}, Net: {}, Tax: {}, Benefit: {}",
                totalGross, totalNet, totalTax, totalBenefit);

        return new YtdSummaryResponse(
                (totalGross),
                (totalNet),
                (totalTax),
                (totalBenefit));
    }
}
