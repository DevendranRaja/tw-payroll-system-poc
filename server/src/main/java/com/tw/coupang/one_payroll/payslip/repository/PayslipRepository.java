package com.tw.coupang.one_payroll.payslip.repository;

import com.tw.coupang.one_payroll.payslip.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
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

    @Query(value = "SELECT * FROM public.payslip p WHERE p.employee_id = :employeeId " +
            "AND EXTRACT(YEAR FROM p.pay_period) = :year " +
            "ORDER BY p.pay_period ASC", nativeQuery = true)
    List<Payslip> findByEmployeeIdAndYear(
            @Param("employeeId") String employeeId,
            @Param("year") int year
    );
}