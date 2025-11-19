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
    void createEmployee_employeeIdAlreadyExists_returnsConflict() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "E004", "Bob", "Johnson", "Sales", "Executive", "bob.johnson@example.com", 4, LocalDate.now()
        );

        when(employeeMasterService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenThrow(new EmployeeConflictException("Employee ID already exists"));

        ResponseEntity<?> response = controller.createEmployee(request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody() instanceof java.util.Map);
        verify(employeeMasterService, times(1)).createEmployee(any(CreateEmployeeRequest.class));
    }

    @Test
    void createEmployee_emailAlreadyExists_returnsConflict() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "E005", "Carol", "Davis", "Marketing", "Coordinator", "carol.davis@example.com", 5, LocalDate.now()
        );

        when(employeeMasterService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenThrow(new EmployeeConflictException("Email already in use"));

        ResponseEntity<?> response = controller.createEmployee(request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody() instanceof java.util.Map);
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

        ResponseEntity<EmployeeMaster> response = (ResponseEntity<EmployeeMaster>) controller.updateEmployee(empId, update);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updated.getEmployeeId(), response.getBody().getEmployeeId());
    }

//    @Test
//    void updateEmployee_notFound_throwsException() {
//        String empId = "E404";
//        UpdateEmployeeRequest update = new UpdateEmployeeRequest("Non", "Exist", "Dept", "Role", "non.exist@example.com", 1, LocalDate.now(), null);
//        when(employeeMasterService.updateEmployee(eq(empId), any(UpdateEmployeeRequest.class))).thenThrow(new IllegalArgumentException("Employee not found"));
//
//        // Global exception handler will catch this
//        assertThrows(IllegalArgumentException.class, () -> controller.updateEmployee(empId, update));
//    }

    @Test
    void updateEmployee_partialUpdate_onlyFirstName() {
        String empId = "E003";
        // Update request with only firstName, other fields are null
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

        when(employeeMasterService.updateEmployee(eq(empId), any(UpdateEmployeeRequest.class))).thenReturn(updated);

        ResponseEntity<?> response = controller.updateEmployee(empId, update);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof EmployeeMaster);
        assertEquals("Alice_Updated", ((EmployeeMaster) response.getBody()).getFirstName());
    }

    @Test
    void updateEmployee_partialUpdate_onlyEmail() {
        String empId = "E003";
        // Update request with only email, other fields are null
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

        when(employeeMasterService.updateEmployee(eq(empId), any(UpdateEmployeeRequest.class))).thenReturn(updated);

        ResponseEntity<?> response = controller.updateEmployee(empId, update);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof EmployeeMaster);
        assertEquals("newemail@example.com", ((EmployeeMaster) response.getBody()).getEmail());
    }
}
