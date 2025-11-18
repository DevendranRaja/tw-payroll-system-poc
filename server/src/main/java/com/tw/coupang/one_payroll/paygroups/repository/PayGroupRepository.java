package com.tw.coupang.one_payroll.paygroups.repository;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PayGroupRepository extends JpaRepository<PayGroup, Integer> {

}
