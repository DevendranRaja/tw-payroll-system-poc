package com.tw.coupang.one_payroll.integration.repository;

import com.tw.coupang.one_payroll.integration.entity.PayrollBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollBatchRepository extends JpaRepository<PayrollBatch, Long> {
    boolean existsByBatchRefId(String batchId);
}