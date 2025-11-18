package com.tw.coupang.one_payroll.EmployeeMaster.Controller;

import com.tw.coupang.one_payroll.EmployeeMaster.Dto.CreateEmployeeRequest;
import com.tw.coupang.one_payroll.EmployeeMaster.Dto.UpdateEmployeeRequest;
import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.EmployeeMaster.Service.EmployeeMasterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

        ResponseEntity<?> response = controller.createEmployee(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Object body = response.getBody();
        assertTrue(body instanceof EmployeeMaster);
        assertEquals(created, (EmployeeMaster) body);
        verify(employeeMasterService, times(1)).createEmployee(any(CreateEmployeeRequest.class));
    }

    @Test
    void updateEmployee_success() {
        String empId = "E003";
        UpdateEmployeeRequest update = new UpdateEmployeeRequest("Alice", "BrownUpdated", "Finance", "Sr Analyst", "alice.brown@example.com", 3, LocalDate.now(), "INACTIVE");
        EmployeeMaster updated = EmployeeMaster.builder()
                .employeeId(empId)
                .firstName(update.getFirstName())
                .lastName(update.getLastName())
                .email(update.getEmail())
                .payGroupId(update.getPayGroupId())
                .status(EmployeeStatus.INACTIVE)
                .build();

        when(employeeMasterService.updateEmployee(eq(empId), any(UpdateEmployeeRequest.class))).thenReturn(updated);

        ResponseEntity<?> response = controller.updateEmployee(empId, update);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof EmployeeMaster);
        assertEquals(updated, (EmployeeMaster) response.getBody());
    }

    @Test
    void updateEmployee_notFound_returnsNotFound() {
        String empId = "E404";
        UpdateEmployeeRequest update = new UpdateEmployeeRequest("Non", "Exist", "Dept", "Role", "non.exist@example.com", 1, LocalDate.now(), null);
        when(employeeMasterService.updateEmployee(eq(empId), any(UpdateEmployeeRequest.class))).thenThrow(new IllegalArgumentException("Employee not found"));

        ResponseEntity<?> response = controller.updateEmployee(empId, update);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof java.util.Map);
        assertEquals("Employee not found", ((java.util.Map<?, ?>) response.getBody()).get("error"));
    }
}
