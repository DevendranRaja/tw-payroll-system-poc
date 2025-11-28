package com.tw.coupang.one_payroll.payroll.repository;

import com.tw.coupang.one_payroll.payroll.entity.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, Integer> {
    List<PayrollRun> findByEmployeeIdOrPayPeriodStartAndPayPeriodEnd(
            String employeeId, LocalDate payPeriodStart, LocalDate payPeriodEnd
    );
}
