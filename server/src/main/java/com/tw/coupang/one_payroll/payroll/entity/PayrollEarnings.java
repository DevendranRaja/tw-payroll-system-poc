package com.tw.coupang.one_payroll.payroll.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "payroll_earnings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayrollEarnings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payroll_earnings_id")
    private Integer payrollEarningsId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "earning_type_id", nullable = false)
    private EarningType earningType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
