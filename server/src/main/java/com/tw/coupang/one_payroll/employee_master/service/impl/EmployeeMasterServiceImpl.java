package com.tw.coupang.one_payroll.employee_master.service.impl;

import com.tw.coupang.one_payroll.employee_master.dto.CreateEmployeeRequest;
import com.tw.coupang.one_payroll.employee_master.dto.UpdateEmployeeRequest;
import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.employee_master.enums.EmployeeStatus;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeConflictException;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.employee_master.repository.EmployeeMasterRepository;
import com.tw.coupang.one_payroll.employee_master.service.EmployeeMasterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeMasterServiceImpl implements EmployeeMasterService {

    private final EmployeeMasterRepository repository;
    private static final String EMPLOYEE_NOT_FOUND_PREFIX = "Employee with ID '";
    private static final String EMPLOYEE_NOT_FOUND_SUFFIX = "' not found";

    public EmployeeMasterServiceImpl(EmployeeMasterRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public EmployeeMaster createEmployee(CreateEmployeeRequest request) {

        if (repository.existsByEmployeeId(request.getEmployeeId())) {
            throw new EmployeeConflictException("Employee ID already exists");
        }

        if (repository.existsByEmail(request.getEmail())) {
            throw new EmployeeConflictException("Email already in use");
        }

        EmployeeMaster employee = EmployeeMaster.builder()
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

        return repository.save(employee);
    }

    @Override
    @Transactional
    public EmployeeMaster updateEmployee(String employeeId, UpdateEmployeeRequest request) {
        EmployeeMaster employee = findEmployeeOrThrow(employeeId);

        updateEmail(employee, request.getEmail());
        updateName(employee, request.getFirstName(), request.getLastName());
        updateDepartmentAndDesignation(employee, request.getDepartment(), request.getDesignation());
        updatePayGroup(employee, request.getPayGroupId());
        updateJoiningDate(employee, request.getJoiningDate());
        updateStatus(employee, request.getStatus());

        return repository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeMaster getEmployeeById(String employeeId) {
        return repository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(EMPLOYEE_NOT_FOUND_PREFIX + employeeId + EMPLOYEE_NOT_FOUND_SUFFIX));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeMaster> getEmployeesByDepartment(String department, boolean includeInactive) {
        if (includeInactive) {
            return repository.findByDepartmentIgnoreCase(department.trim());
        }
        return repository.findByDepartmentIgnoreCaseAndStatus(department.trim(), EmployeeStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void deleteEmployee(String employeeId) {
        EmployeeMaster employee = repository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(EMPLOYEE_NOT_FOUND_PREFIX + employeeId + EMPLOYEE_NOT_FOUND_SUFFIX));
        employee.setStatus(EmployeeStatus.INACTIVE);
        repository.save(employee);
    }

    private EmployeeMaster findEmployeeOrThrow(String employeeId) {
        return repository.findById(employeeId)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(EMPLOYEE_NOT_FOUND_PREFIX + employeeId + EMPLOYEE_NOT_FOUND_SUFFIX));
    }

    private void updateEmail(EmployeeMaster employee, String email) {
        if (email != null && !email.isBlank()) {
            if (!email.equals(employee.getEmail()) && repository.existsByEmail(email)) {
                throw new EmployeeConflictException("Email already in use");
            }
            employee.setEmail(email);
        }
    }

    private void updateName(EmployeeMaster employee, String firstName, String lastName) {
        if (firstName != null && !firstName.isBlank()) employee.setFirstName(firstName);
        if (lastName != null && !lastName.isBlank()) employee.setLastName(lastName);
    }

    private void updateDepartmentAndDesignation(EmployeeMaster employee, String department, String designation) {
        if (department != null && !department.isBlank()) employee.setDepartment(department);
        if (designation != null && !designation.isBlank()) employee.setDesignation(designation);
    }

    private void updatePayGroup(EmployeeMaster employee, Integer payGroupId) {
        if (payGroupId != null) employee.setPayGroupId(payGroupId);
    }

    private void updateJoiningDate(EmployeeMaster employee, LocalDate joiningDate) {
        if (joiningDate != null) employee.setJoiningDate(joiningDate);
    }

    private void updateStatus(EmployeeMaster employee, String statusStr) {
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                EmployeeStatus status = EmployeeStatus.valueOf(statusStr.trim().toUpperCase());
                employee.setStatus(status);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid status value: " + statusStr);
            }
        }
    }
}
