package com.tw.coupang.one_payroll.payslipEss.payrollmock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, Integer>
{
    @Query(value = "SELECT * FROM public.payroll_run pr WHERE pr.employeeId = :employeeId " +
            "AND TO_CHAR(pr.pay_period_end, 'YYYY-MM') = :yearMonth " +
            "ORDER BY pr.payPeriodEnd DESC",
            nativeQuery = true)
    Optional<PayrollRun> findPayrollForEmployeeIdAndPayPeriod(
            @Param("employeeId") String employeeId,
            @Param("yearMonth") String yearMonth
    );
}