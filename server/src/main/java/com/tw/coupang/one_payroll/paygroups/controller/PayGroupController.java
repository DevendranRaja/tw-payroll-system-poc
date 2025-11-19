package com.tw.coupang.one_payroll.paygroups.controller;

import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupCreateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupUpdateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupResponse;
import com.tw.coupang.one_payroll.paygroups.service.PayGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay-groups")
public class PayGroupController {

    private final PayGroupService payGroupService;

    public PayGroupController(PayGroupService payGroupService) {
        this.payGroupService = payGroupService;
    }

    @PostMapping
    public ResponseEntity<PayGroupResponse> createPayGroup(@Valid @RequestBody PayGroupCreateRequest request) {
        PayGroupResponse response = payGroupService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PayGroupResponse> updatePayGroup(
            @PathVariable Integer id,
            @Valid @RequestBody PayGroupUpdateRequest request
    ) {
        PayGroupResponse response = payGroupService.update(id, request);
        return ResponseEntity.ok(response);
    }
}
