package com.tw.coupang.one_payroll.payroll.repository;

import com.tw.coupang.one_payroll.payroll.entity.PayrollRun;
import com.tw.coupang.one_payroll.payroll.enums.PayrollStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, Integer> {

    @Query("""
    SELECT p FROM PayrollRun p
    WHERE (:employeeId IS NULL OR p.employeeId = :employeeId)
      AND p.payPeriodStart >= COALESCE(:startDate, p.payPeriodStart)
      AND p.payPeriodEnd <= COALESCE(:endDate, p.payPeriodEnd)
    """)
    List<PayrollRun> findByEmployeeIdOrPayPeriodStartAndPayPeriodEnd(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "SELECT * FROM public.payroll_run pr WHERE pr.employee_id = :employeeId " +
            "AND TO_CHAR(pr.pay_period_end, 'YYYY-MM') = :yearMonth " +
            "ORDER BY pr.pay_period_end DESC",
            nativeQuery = true)
    Optional<PayrollRun> findPayrollForEmployeeIdAndPayPeriod(
            @Param("employeeId") String employeeId,
            @Param("yearMonth") String yearMonth
    );

    // Fetch the next 100 records that need to be sent
    List<PayrollRun> findTop5ByStatus(PayrollStatus status);

}
