package com.tw.coupang.one_payroll.EmployeeMaster.Controller;

import com.tw.coupang.one_payroll.EmployeeMaster.Dto.CreateEmployeeRequest;
import com.tw.coupang.one_payroll.EmployeeMaster.Dto.UpdateEmployeeRequest;
import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.EmployeeMaster.Exception.EmployeeConflictException;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /** -------------------------------------------------
     * CREATE EMPLOYEE TESTS
     * ------------------------------------------------- */

    @Test
    void createEmployee_success() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "E003", "Alice", "Brown", "Finance", "Analyst",
                "alice.brown@example.com", 3, LocalDate.now()
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

        when(employeeMasterService.createEmployee(request)).thenReturn(created);

        ResponseEntity<?> response = controller.createEmployee(request);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(created, response.getBody());
        verify(employeeMasterService, times(1)).createEmployee(request);
    }

    @Test
    void createEmployee_conflictException_propagatesToGlobalHandler() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "E004", "Bob", "Johnson", "Sales", "Executive",
                "bob.johnson@example.com", 4, LocalDate.now()
        );

        when(employeeMasterService.createEmployee(request))
                .thenThrow(new EmployeeConflictException("Employee ID already exists"));

        // Controller does NOT handle the exception → GlobalExceptionHandler will
        assertThrows(EmployeeConflictException.class,
                () -> controller.createEmployee(request));

        verify(employeeMasterService, times(1)).createEmployee(request);
    }

    /** -------------------------------------------------
     * UPDATE EMPLOYEE TESTS
     * ------------------------------------------------- */

    @Test
    void updateEmployee_success() {
        String empId = "E003";
        UpdateEmployeeRequest update = new UpdateEmployeeRequest(
                "Alice", "BrownUpdated", "Finance", "Sr Analyst",
                "alice.brown@example.com", 3, LocalDate.now(), "INACTIVE"
        );

        EmployeeMaster updated = EmployeeMaster.builder()
                .employeeId(empId)
                .firstName(update.getFirstName())
                .lastName(update.getLastName())
                .email(update.getEmail())
                .payGroupId(update.getPayGroupId())
                .status(EmployeeStatus.INACTIVE)
                .build();

        when(employeeMasterService.updateEmployee(empId, update)).thenReturn(updated);

        ResponseEntity<?> response = controller.updateEmployee(empId, update);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(updated, response.getBody());
    }

    @Test
    void updateEmployee_notFound_propagatesToGlobalHandler() {
        String empId = "E404";
        UpdateEmployeeRequest update = new UpdateEmployeeRequest(
                "Non", "Exist", "Dept", "Role",
                "non.exist@example.com", 1, LocalDate.now(), null
        );

        when(employeeMasterService.updateEmployee(empId, update))
                .thenThrow(new IllegalArgumentException("Employee not found"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.updateEmployee(empId, update));
    }

    @Test
    void updateEmployee_partialUpdate_firstNameOnly() {
        String empId = "E003";
        UpdateEmployeeRequest update = new UpdateEmployeeRequest();
        update.setFirstName("Alice_Updated");

        EmployeeMaster updated = EmployeeMaster.builder()
                .employeeId(empId)
                .firstName("Alice_Updated")
                .lastName("Brown")
                .email("alice.brown@example.com")
                .payGroupId(3)
                .status(EmployeeStatus.ACTIVE)
                .build();

        when(employeeMasterService.updateEmployee(empId, update)).thenReturn(updated);

        ResponseEntity<?> response = controller.updateEmployee(empId, update);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Alice_Updated",
                ((EmployeeMaster) response.getBody()).getFirstName());
    }

    @Test
    void updateEmployee_partialUpdate_emailOnly() {
        String empId = "E003";

        UpdateEmployeeRequest update = new UpdateEmployeeRequest();
        update.setEmail("newemail@example.com");

        EmployeeMaster updated = EmployeeMaster.builder()
                .employeeId(empId)
                .firstName("Alice")
                .lastName("Brown")
                .email("newemail@example.com")
                .payGroupId(3)
                .status(EmployeeStatus.ACTIVE)
                .build();

        when(employeeMasterService.updateEmployee(empId, update)).thenReturn(updated);

        ResponseEntity<?> response = controller.updateEmployee(empId, update);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("newemail@example.com",
                ((EmployeeMaster) response.getBody()).getEmail());
    }
}
