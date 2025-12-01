package com.tw.coupang.one_payroll.payroll.repository;

import com.tw.coupang.one_payroll.payroll.entity.PayrollRun;
import com.tw.coupang.one_payroll.payroll.enums.PayrollStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

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

    // Fetch the next 100 records that need to be sent
    List<PayrollRun> findTop100ByStatus(PayrollStatus status);

}
