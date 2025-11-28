package com.tw.coupang.one_payroll.integration.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_batch_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollBatchLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private String batchRefId;
    private String employeeId;
    private String status;
    private LocalDateTime timestamp;
}
