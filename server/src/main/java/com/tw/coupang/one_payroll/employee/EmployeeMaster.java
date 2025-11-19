package com.tw.coupang.one_payroll.employee;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "employee_master")
public class EmployeeMaster {

    @Id
    @Column(name = "employee_id", length = 10)
    private String employeeId;

    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Column(length = 50, nullable = false)
    private String department;

    @Column(length = 50, nullable = false)
    private String designation;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "pay_group_id", nullable = false)
    private Integer payGroupId;

    @Column(name = "status")
    private String status; // maps to employee_status enum

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors, getters, setters, equals/hashCode

    public EmployeeMaster() {}

    // getters and setters...
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getPayGroupId() { return payGroupId; }
    public void setPayGroupId(Integer payGroupId) { this.payGroupId = payGroupId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmployeeMaster that = (EmployeeMaster) o;
        return Objects.equals(employeeId, that.employeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId);
    }
}