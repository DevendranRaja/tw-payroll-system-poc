package com.tw.coupang.one_payroll.integration.repository;

import com.tw.coupang.one_payroll.integration.entity.PayrollBatchLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollBatchLogRepository extends JpaRepository<PayrollBatchLog, Long> {

    Page<PayrollBatchLog> findByBatchRefId(String batchRefId, Pageable pageable);

    Page<PayrollBatchLog> findByEmployeeId(String employeeId, Pageable pageable);

    Page<PayrollBatchLog> findByBatchRefIdAndEmployeeId(String batchRefId, String employeeId, Pageable pageable);
}