package com.tw.coupang.one_payroll.payroll.repository;

import com.tw.coupang.one_payroll.payroll.entity.EarningType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EarningTypeRepository extends JpaRepository<EarningType, Integer> {

    Optional<EarningType> findByName(String name);
}

