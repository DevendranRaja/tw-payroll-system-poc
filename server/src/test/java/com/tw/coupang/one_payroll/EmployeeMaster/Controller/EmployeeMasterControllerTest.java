package com.tw.coupang.one_payroll.EmployeeMaster.Controller;

import com.tw.coupang.one_payroll.EmployeeMaster.Dto.CreateEmployeeRequest;
import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.EmployeeMaster.Service.EmployeeMasterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeMasterControllerTest {

    @Mock
    private EmployeeMasterService employeeMasterService;

    @InjectMocks
    private EmployeeMasterController controller;

    @SuppressWarnings("resource")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createEmployee_success() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "E003", "Alice", "Brown", "Finance", "Analyst", "alice.brown@example.com", 3, LocalDate.now()
        );

        EmployeeMaster created = EmployeeMaster.builder()
                .employeeId(request.getEmployeeId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .email(request.getEmail())
                .payGroupId(request.getPayGroupId())
                .status(EmployeeStatus.ACTIVE)
                .joiningDate(request.getJoiningDate())
                .build();

        when(employeeMasterService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(created);

        ResponseEntity<EmployeeMaster> response = (ResponseEntity<EmployeeMaster>) controller.createEmployee(request);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(created, response.getBody());
        verify(employeeMasterService, times(1)).createEmployee(any(CreateEmployeeRequest.class));
    }
}
