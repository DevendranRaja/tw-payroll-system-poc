package com.tw.coupang.one_payroll.payroll.repository;

import com.tw.coupang.one_payroll.payroll.entity.PayrollEarnings;
import com.tw.coupang.one_payroll.payroll.entity.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollEarningsRepository extends JpaRepository<PayrollEarnings, Integer> {

    List<PayrollEarnings> findByPayrollRun(PayrollRun payrollRun);
}
