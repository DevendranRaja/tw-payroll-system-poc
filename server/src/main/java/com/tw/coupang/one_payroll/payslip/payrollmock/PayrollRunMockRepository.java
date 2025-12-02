package com.tw.coupang.one_payroll.payslip.payrollmock;

import com.tw.coupang.one_payroll.payroll.entity.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayrollRunMockRepository extends JpaRepository<PayrollRun, Integer>
{
    @Query(value = "SELECT * FROM public.payroll_run pr WHERE pr.employee_id = :employeeId " +
            "AND TO_CHAR(pr.pay_period_end, 'YYYY-MM') = :yearMonth " +
            "ORDER BY pr.pay_period_end DESC",
            nativeQuery = true)
    Optional<PayrollRun> findPayrollForEmployeeIdAndPayPeriod(
            @Param("employeeId") String employeeId,
            @Param("yearMonth") String yearMonth
    );
}