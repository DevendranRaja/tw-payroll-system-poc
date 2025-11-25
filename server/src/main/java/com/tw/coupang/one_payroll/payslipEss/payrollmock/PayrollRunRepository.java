package com.tw.coupang.one_payroll.payslipEss.payrollmock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, Integer> {

    Optional<PayrollRun> findByEmployeeIdAndPayPeriodEnd(String employeeId, LocalDate payPeriodEnd);
}
