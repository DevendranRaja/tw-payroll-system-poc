package com.tw.coupang.one_payroll.paygroups.repository;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayGroupRepository extends JpaRepository<PayGroup, Integer> {
    boolean existsByGroupNameIgnoreCase(String groupName);

    List<PayGroup> findByPaymentCycle(PaymentCycle paymentCycle);
}
