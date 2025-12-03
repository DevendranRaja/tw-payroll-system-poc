package com.tw.coupang.one_payroll.payperiod.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "pay_period",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_pay_period_group_start_end", columnNames = {"pay_group_id", "period_start_date", "period_end_date"})
    },
    indexes = {
        @Index(name = "idx_pay_period_group", columnList = "pay_group_id")
    }
)
public class PayPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pay_period_id")
    private Integer id;

    @Column(name = "pay_group_id", nullable = false)
    private Integer payGroupId;

    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate;

    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate;

    @Column(name = "range", nullable = false, length = 20)
    private String range;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
