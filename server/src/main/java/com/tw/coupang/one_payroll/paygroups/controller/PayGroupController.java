package com.tw.coupang.one_payroll.paygroups.controller;

import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupCreateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupUpdateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupResponse;
import com.tw.coupang.one_payroll.paygroups.service.PayGroupService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/pay-groups")
public class PayGroupController {

    private final PayGroupService payGroupService;

    public PayGroupController(PayGroupService payGroupService) {
        this.payGroupService = payGroupService;
    }

    @PostMapping
    public ResponseEntity<PayGroupResponse> createPayGroup(@Valid @RequestBody PayGroupCreateRequest request) {
        log.info("Received request to create pay group: {}", request);

        PayGroupResponse response = payGroupService.create(request);

        log.info("Pay group created successfully with ID: {}", response.getPayGroupId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PayGroupResponse> updatePayGroup(
            @PathVariable Integer id,
            @Valid @RequestBody PayGroupUpdateRequest request
    ) {
        log.info("Received request to update pay group ID {}: {}", id, request);

        PayGroupResponse response = payGroupService.update(id, request);

        log.info("Pay group ID {} updated successfully", id);
        return ResponseEntity.ok(response);
    }
}
