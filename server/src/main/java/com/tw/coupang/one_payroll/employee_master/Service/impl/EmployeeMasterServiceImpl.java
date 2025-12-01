package com.tw.coupang.one_payroll.employee_master.Service.impl;

import com.tw.coupang.one_payroll.employee_master.Dto.CreateEmployeeRequest;
import com.tw.coupang.one_payroll.employee_master.Dto.UpdateEmployeeRequest;
import com.tw.coupang.one_payroll.employee_master.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.employee_master.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeConflictException;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.employee_master.Repository.EmployeeMasterRepository;
import com.tw.coupang.one_payroll.employee_master.Service.EmployeeMasterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeMasterServiceImpl implements EmployeeMasterService {

    private final EmployeeMasterRepository repository;

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
        EmployeeMaster employee = repository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with ID '" + employeeId + "' not found"));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!employee.getEmail().equals(request.getEmail()) && repository.existsByEmail(request.getEmail())) {
                throw new EmployeeConflictException("Email already in use");
            }
            employee.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            employee.setLastName(request.getLastName());
        }
        if (request.getDepartment() != null && !request.getDepartment().isBlank()) {
            employee.setDepartment(request.getDepartment());
        }
        if (request.getDesignation() != null && !request.getDesignation().isBlank()) {
            employee.setDesignation(request.getDesignation());
        }
        if (request.getPayGroupId() != null) {
            employee.setPayGroupId(request.getPayGroupId());
        }
        if (request.getJoiningDate() != null) {
            employee.setJoiningDate(request.getJoiningDate());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                EmployeeStatus status = EmployeeStatus.valueOf(request.getStatus().trim().toUpperCase());
                employee.setStatus(status);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid status value: " + request.getStatus());
            }
        }
        return repository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeMaster getEmployeeById(String employeeId) {
        return repository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with ID '" + employeeId + "' not found"));
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
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with ID '" + employeeId + "' not found"));
        employee.setStatus(EmployeeStatus.INACTIVE);
        repository.save(employee);
    }
}
