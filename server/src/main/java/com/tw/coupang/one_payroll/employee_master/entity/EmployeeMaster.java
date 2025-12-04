package com.tw.coupang.one_payroll.employee_master.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.tw.coupang.one_payroll.employee_master.enums.EmployeeStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeMaster {

    @Id
    @Column(name = "employee_id", length = 10)
    private String employeeId;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "department", length = 50)
    private String department;

    @Column(name = "designation", length = 50)
    private String designation;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "pay_group_id", nullable = false)
    private Integer payGroupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EmployeeStatus status;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "base_salary", precision = 15, scale = 2)
    private BigDecimal baseSalary;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

