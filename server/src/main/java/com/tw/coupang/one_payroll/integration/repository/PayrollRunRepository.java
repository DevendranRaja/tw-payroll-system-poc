package com.tw.coupang.one_payroll.integration.repository;

import com.tw.coupang.one_payroll.integration.entity.PayrollRun;
import com.tw.coupang.one_payroll.integration.enums.PayrollStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, Integer> {

    // Fetch the next 100 records that need to be sent
    List<PayrollRun> findTop5ByStatus(PayrollStatus status);
    List<PayrollRun> findTop5ByStatusNot(PayrollStatus status);

}
