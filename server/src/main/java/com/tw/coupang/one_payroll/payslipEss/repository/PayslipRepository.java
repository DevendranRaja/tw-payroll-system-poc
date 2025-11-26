package com.tw.coupang.one_payroll.payslipEss.repository;

import com.tw.coupang.one_payroll.payslipEss.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PayslipRepository extends JpaRepository<Payslip, Long>
{
    Optional<Payslip> findByEmployeeIdAndPayPeriod(String employeeId, LocalDate payPeriod);
}

