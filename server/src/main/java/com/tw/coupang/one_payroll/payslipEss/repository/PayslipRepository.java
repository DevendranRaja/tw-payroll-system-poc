package com.tw.coupang.one_payroll.payslipEss.repository;

import com.tw.coupang.one_payroll.payslipEss.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PayslipRepository extends JpaRepository<Payslip, Long>
{
    Optional<Payslip> findByEmployeeIdAndPayPeriod(String employeeId, LocalDate payPeriod);

    @Query(value = "SELECT * FROM public.payslip p WHERE p.employee_id  = :employeeId " +
            "AND TO_CHAR(p.pay_period, 'YYYY-MM') = :yearMonth", nativeQuery = true)
    Optional<Payslip> findByEmployeeIdAndYearMonth(
            @Param("employeeId") String employeeId,
            @Param("yearMonth") String yearMonth
    );
}