package com.tw.coupang.one_payroll.payslipEss.entity;

import com.tw.coupang.one_payroll.payslipEss.dto.PayslipComponentDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "payslip")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payslip_id")
    private Long id;

    @Column(name = "employee_id")
    private String employeeId;

    @Column(name = "payroll_id")
    private Long payrollId;

    @Column(name = "pay_period")
    private LocalDate payPeriod;

    @Column(name = "gross_pay")
    private BigDecimal grossPay;

    @Column(name = "net_pay")
    private BigDecimal netPay;

    @Column(name = "tax")
    private BigDecimal tax;

    @Column(name = "benefits")
    private BigDecimal benefits;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // JSON columns for break-up
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "earnings_json", columnDefinition = "jsonb")
    private List<PayslipComponentDto> earnings;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "deductions_json", columnDefinition = "jsonb")
    private List<PayslipComponentDto> deductions;

}

