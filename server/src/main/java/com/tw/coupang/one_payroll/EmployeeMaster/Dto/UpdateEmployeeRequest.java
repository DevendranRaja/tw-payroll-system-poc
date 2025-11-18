package com.tw.coupang.one_payroll.EmployeeMaster.Dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequest {
    private String firstName;
    private String lastName;
    private String department;
    private String designation;
    @Email private String email;
    private Integer payGroupId;
    private LocalDate joiningDate;
    private String status;
}
