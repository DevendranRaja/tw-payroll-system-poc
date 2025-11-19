package com.tw.coupang.one_payroll.EmployeeMaster.Dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRequest {

    @NotBlank
    private String employeeId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String department;

    private String designation;

    @Email
    @NotBlank
    private String email;

    @NotNull
    private Integer payGroupId; //Once PayGroup entity is created, this can be validated with valid existing PayGroup IDs.

    private LocalDate joiningDate;
}


