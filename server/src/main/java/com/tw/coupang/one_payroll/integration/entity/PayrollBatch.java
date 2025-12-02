package com.tw.coupang.one_payroll.integration.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_batch")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String batchRefId;

    private String payPeriod;
    private BigDecimal totalAmount;
    private String status; // PENDING, SUCCESS, RETRY, FAILED
    @Column(name = "log_message")
    private String logMessage;
    @Column(name = "employee_count")
    private Integer employeeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
