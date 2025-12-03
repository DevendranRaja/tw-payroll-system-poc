package com.tw.coupang.one_payroll.payperiod.repository;

import com.tw.coupang.one_payroll.payperiod.entity.PayPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface PayPeriodRepository extends JpaRepository<PayPeriod, Integer> {

    @Query("""
        select case when count(p) > 0 then true else false end
        from PayPeriod p
        where p.payGroupId = :payGroupId
          and p.periodStartDate <= :endDate
          and p.periodEndDate >= :startDate
    """)
    boolean existsOverlappingPeriod(@Param("payGroupId") Integer payGroupId,
                                    @Param("periodStartDate") LocalDate periodStartDate,
                                    @Param("periodEndDate") LocalDate periodEndDate);
}
