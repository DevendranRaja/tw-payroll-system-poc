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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class YtdSummaryServiceImplTest
{
    private EmployeeMaster employee;
    private List<Payslip> mockPayslips;
    private String employeeId;
    private int year;

    @Mock
    PayslipRepository payslipRepository;

    @Mock
    private EmployeeMasterRepository employeeMasterRepository;

    @InjectMocks
    YtdSummaryServiceImpl ytdSummaryService;

    @BeforeEach
    void setUp()
    {
        employeeId="E001";
        year=2025;
        mockPayslips = new ArrayList<>();

        employee = new EmployeeMaster();
        employee.setEmployeeId(employeeId);
        employee.setFirstName("Jin");
        employee.setLastName("Park");
        employee.setDepartment("Finance");
        employee.setDesignation("Analyst");
        employee.setPayGroupId(1);
        employee.setStatus(EmployeeStatus.ACTIVE);

        // Create 3 months of payslips
        mockPayslips.add(createPayslip(LocalDate.of(2025, 1, 31),
                new BigDecimal("5000.00"), new BigDecimal("4500.00"),
                new BigDecimal("400.00"), new BigDecimal("100.00")));

        mockPayslips.add(createPayslip(LocalDate.of(2025, 2, 28),
                new BigDecimal("5500.00"), new BigDecimal("5000.00"),
                new BigDecimal("400.00"), new BigDecimal("100.00")));

        mockPayslips.add(createPayslip(LocalDate.of(2025, 3, 31),
                new BigDecimal("6000.00"), new BigDecimal("5400.00"),
                new BigDecimal("500.00"), new BigDecimal("100.00")));
    }

    // Helper method to create test payslips
    private Payslip createPayslip(LocalDate payPeriod,
                                  BigDecimal grossPay, BigDecimal netPay,
                                  BigDecimal tax, BigDecimal benefits)
    {
        return Payslip.builder()
                .employeeId(employeeId)
                .payPeriod(payPeriod)
                .grossPay(grossPay)
                .netPay(netPay)
                .tax(tax)
                .benefits(benefits)
                .build();
    }

    @Test
    void shouldThrowExceptionWhenEmployeeIdIsMissing()
    {
        when(employeeMasterRepository.findById(employeeId))
                .thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
                () -> ytdSummaryService.getYtdSummaryWithBreakdown(employeeId, year));
    }

    @Test
    void shouldThrowExceptionWhenEmployeeIsInactive()
    {
        employee.setStatus(EmployeeStatus.INACTIVE);
        when(employeeMasterRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));

        assertThrows(EmployeeNotFoundException.class,
                () -> ytdSummaryService.getYtdSummaryWithBreakdown(employeeId, year));

    }

//    @Test
//    void shouldReturnEmployeeDetailsCorrectly()
//    {
//        when(payslipRepository.findByEmployeeIdAndYear(employeeId, year))
//                .thenReturn(mockPayslips);
//
//        YtdSummaryForPdfDto ytdSummary = ytdSummaryService.getYtdSummaryWithBreakdown(employeeId, year);
//
//        assertNotNull(ytdSummary);
//        assertEquals("E001", ytdSummary.employeeId());
//        assertEquals("Jin Park", ytdSummary.employeeName());
//        assertEquals("Finance", ytdSummary.department());
//        assertEquals("Analyst", ytdSummary.designation());
//    }


    @Test
    void shouldReturnMonthlyBreakdownWithCorrectMonths()
    {
        when(employeeMasterRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));

        when(payslipRepository.findByEmployeeIdAndYear(employeeId, year))
                .thenReturn(mockPayslips);

        YtdSummaryForPdfDto ytdSummary = ytdSummaryService.getYtdSummaryWithBreakdown(employeeId, year);

        assertNotNull(ytdSummary);
        assertEquals("E001", ytdSummary.employeeId());
        assertEquals(2025, ytdSummary.year());
        assertEquals(3, ytdSummary.monthlyBreakdown().size());

        // Verify monthly breakdown
        MonthlyPayslipSummaryDto janDetails = ytdSummary.monthlyBreakdown().get("JANUARY");
        assertEquals(1, janDetails.monthNumber());
        assertEquals(2025, janDetails.year());
        assertEquals(new BigDecimal("5000.00"), janDetails.grossPay());
        assertEquals(new BigDecimal("4500.00"), janDetails.netPay());
        assertEquals(new BigDecimal("400.00"), janDetails.deductions());
        assertEquals(new BigDecimal("100.00"), janDetails.benefit());

        MonthlyPayslipSummaryDto febDetails = ytdSummary.monthlyBreakdown().get("FEBRUARY");
        assertEquals(2, febDetails.monthNumber());
        assertEquals(new BigDecimal("5500.00"), febDetails.grossPay());

        MonthlyPayslipSummaryDto marchDetails = ytdSummary.monthlyBreakdown().get("MARCH");
        assertEquals(3, marchDetails.monthNumber());
        assertEquals(new BigDecimal("6000.00"), marchDetails.grossPay());
    }

    @Test
    void shouldCalculateYtdSummaryTotalsCorrectly()
    {
        when(employeeMasterRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));

        when(payslipRepository.findByEmployeeIdAndYear(employeeId, year))
                .thenReturn(mockPayslips);

        YtdSummaryForPdfDto ytdSummary = ytdSummaryService.getYtdSummaryWithBreakdown(employeeId, year);
        YtdSummaryResponse ytdTotals = ytdSummary.ytdTotals();


        assertNotNull(ytdTotals);
        assertEquals(new BigDecimal("16500.00"), ytdTotals.totalGross());
        assertEquals(new BigDecimal("14900.00"), ytdTotals.totalNet());
        assertEquals(new BigDecimal("1300.00"), ytdTotals.totalDeductions());
        assertEquals(new BigDecimal("300.00"), ytdTotals.totalBenefit());
    }

}
