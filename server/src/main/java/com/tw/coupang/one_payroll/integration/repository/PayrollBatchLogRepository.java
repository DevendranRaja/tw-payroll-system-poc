package com.tw.coupang.one_payroll.integration.repository;

import com.tw.coupang.one_payroll.integration.entity.PayrollBatchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollBatchLogRepository extends JpaRepository<PayrollBatchLog, Long> {}
