package com.tw.coupang.one_payroll.error.entity;

import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "error_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorLog
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "error_id")
    private Long errorId;

    @Column(name = "module_name", length = 50)
    private String moduleName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private EmployeeMaster employee;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "error_time", insertable = false, updatable = false)
    private LocalDateTime errorTime;

    @Column(name = "resolved", insertable = false)
    private Boolean resolved;

    @Transient
    public String getEmployeeId() {
        return employee != null ? employee.getEmployeeId() : null;
    }

    public void setEmployeeId(String employeeId) {
        if (employee == null)
            employee = new EmployeeMaster();
        employee.setEmployeeId(employeeId);
    }

}
