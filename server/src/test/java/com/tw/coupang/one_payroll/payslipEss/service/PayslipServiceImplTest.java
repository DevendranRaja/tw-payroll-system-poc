package com.tw.coupang.one_payroll.payslipEss.service;

import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.EmployeeMaster.Exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.EmployeeMaster.Repository.EmployeeMasterRepository;
import com.tw.coupang.one_payroll.payslipEss.dto.PayslipMetadataDTO;
import com.tw.coupang.one_payroll.payslipEss.payrollmock.PayrollRun;
import com.tw.coupang.one_payroll.payslipEss.payrollmock.PayrollRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PayslipServiceImplTest {

    private EmployeeMaster employee;
    private PayrollRun payroll;
    String employeeId;
    LocalDate payPeriod;

    @InjectMocks
    private PayslipServiceImpl payslipService;

    @Mock
    private PayrollRunRepository payrollRunRepository;

    @Mock
    private EmployeeMasterRepository employeeMasterRepository;


    @BeforeEach
    void setUp() {
        employeeId = "E001";
        payPeriod = LocalDate.of(2025, 10, 31);

        employee = new EmployeeMaster();
        employee.setEmployeeId(employeeId);
        employee.setFirstName("Jin");
        employee.setLastName("Park");
        employee.setDepartment("Finance");
        employee.setDesignation("Analyst");
        employee.setPayGroupId(1);
        employee.setStatus(EmployeeStatus.ACTIVE);


        payroll = new PayrollRun();
        payroll.setPayrollId(1);
        payroll.setEmployeeId(employeeId);
        payroll.setPayPeriodStart(LocalDate.of(2025, 10, 1));
        payroll.setPayPeriodEnd(LocalDate.of(2025, 10, 31));
        payroll.setGrossPay(new BigDecimal("5000.00"));
        payroll.setTaxDeduction(new BigDecimal("500.00"));
        payroll.setBenefitAddition(new BigDecimal("250.00"));
        payroll.setNetPay(new BigDecimal("4750.00"));
        payroll.setStatus(PayrollRun.PayrollStatus.PROCESSED);
    }

    @Test
    void generatePaySlipMetaDataHappyPath() {
        // This is a placeholder for actual test implementation.
        // You would typically call the method from PayslipServiceImpl
        // and assert the results here.
    }

    @Test
    void shouldThrowExceptionWhenPayrollIsMissing() {
        when(payrollRunRepository.findByEmployeeIdAndPayPeriodEnd(employeeId, payPeriod))
                .thenReturn(Optional.empty());

        when(employeeMasterRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));

        assertThrows(IllegalStateException.class,
                () -> payslipService.generatePayslipMetadata(employeeId, payPeriod));
    }

    @Test
    void shouldThrowExceptionWhenEmployeeIdIsMissing() {
        when(employeeMasterRepository.findById(employeeId))
                .thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
                () -> payslipService.generatePayslipMetadata(employeeId, payPeriod));

    }

    @Test
    void shouldThrowExceptionWhenEmployeeIsInactive()
    {
        employee.setStatus(EmployeeStatus.INACTIVE);
        when(employeeMasterRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));

        assertThrows(EmployeeNotFoundException.class,
                () -> payslipService.generatePayslipMetadata(employeeId, payPeriod));
    }

}
