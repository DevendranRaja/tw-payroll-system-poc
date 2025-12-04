package com.tw.coupang.one_payroll.payroll.repository;

import com.tw.coupang.one_payroll.payroll.entity.PayrollDeductions;
import com.tw.coupang.one_payroll.payroll.entity.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollDeductionsRepository extends JpaRepository<PayrollDeductions, Integer> {

    List<PayrollDeductions> findByPayrollRun(PayrollRun payrollRun);
}
