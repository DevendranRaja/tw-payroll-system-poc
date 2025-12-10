package com.tw.coupang.one_payroll.payroll.repository;

import com.tw.coupang.one_payroll.payroll.entity.DeductionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeductionTypeRepository extends JpaRepository<DeductionType, Integer> {

    Optional<DeductionType> findByName(String name);
}

