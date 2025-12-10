package com.tw.coupang.one_payroll.payperiod.repository;

import com.tw.coupang.one_payroll.payperiod.entity.PayPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PayPeriodRepository extends JpaRepository<PayPeriod, Integer> {

    @Query("""
           select case when count(p) > 0 then true else false end
           from PayPeriod p
           where p.payGroupId = :payGroupId
             and p.periodStartDate <= :periodEndDate
             and p.periodEndDate >= :periodStartDate
           """)
    boolean existsOverlappingPeriod(@Param("payGroupId") Integer payGroupId,
                                    @Param("periodStartDate") LocalDate periodStartDate,
                                    @Param("periodEndDate") LocalDate periodEndDate);

    @Query("""
           select p.id
           from PayPeriod p
           where p.payGroupId = :payGroupId
             and p.periodStartDate = :periodStartDate
             and p.periodEndDate = :periodEndDate
           """)
    Optional<Integer> findPayPeriodId(@Param("payGroupId") Integer payGroupId,
                                      @Param("periodStartDate") LocalDate periodStartDate,
                                      @Param("periodEndDate") LocalDate periodEndDate);
}
