package com.tw.coupang.one_payroll.payslipEss.repository;

import com.tw.coupang.one_payroll.payslipEss.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface PayslipRepository extends JpaRepository<Payslip, Long>
{
    Payslip findByEmployeeIdAndPayPeriod(String employeeId, LocalDate payPeriod);
}

