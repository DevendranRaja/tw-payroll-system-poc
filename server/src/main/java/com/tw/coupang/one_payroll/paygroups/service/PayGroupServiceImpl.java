package com.tw.coupang.one_payroll.paygroups.service;

import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupCreateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupUpdateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupResponse;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.repository.PayGroupRepository;
import com.tw.coupang.one_payroll.paygroups.validator.PayGroupValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayGroupServiceImpl implements PayGroupService {

    private final PayGroupRepository payGroupRepository;
    private final PayGroupValidator payGroupValidator;

    public PayGroupServiceImpl(PayGroupRepository payGroupRepository, PayGroupValidator payGroupValidator) {
        this.payGroupRepository = payGroupRepository;
        this.payGroupValidator = payGroupValidator;
    }

    @Transactional
    @Override
    public PayGroupResponse create(PayGroupCreateRequest request) {
        String name = request.getGroupName().trim();

        payGroupValidator.validateDuplicateName(name);

        PayGroup payGroup = buildPayGroup(request, name);

        PayGroup savedPayGroup = payGroupRepository.save(payGroup);

        return buildPayGroupResponse(savedPayGroup.getId());
    }

    @Transactional
    @Override
    public PayGroupResponse update(Integer id, PayGroupUpdateRequest request) {
        PayGroup existing = payGroupValidator.validatePayGroupExists(id);

        String newName = request.getGroupName() != null ? request.getGroupName().trim() : null;

        if (newName != null) {
            payGroupValidator.validateDuplicateName(newName);
        }

        PayGroup updated = PayGroup.builder()
                .id(existing.getId())
                .groupName(resolve(newName, existing.getGroupName()))
                .paymentCycle(resolve(request.getPaymentCycle(), existing.getPaymentCycle()))
                .baseTaxRate(resolve(request.getBaseTaxRate(), existing.getBaseTaxRate()))
                .benefitRate(resolve(request.getBenefitRate(), existing.getBenefitRate()))
                .deductionRate(resolve(request.getDeductionRate(), existing.getDeductionRate()))
                .createdAt(existing.getCreatedAt())
                .build();

        updated = payGroupRepository.save(updated);

        return buildPayGroupResponse(updated.getId());
    }

    private PayGroup buildPayGroup(PayGroupCreateRequest request, String name) {
        return PayGroup.builder()
                .groupName(name)
                .paymentCycle(request.getPaymentCycle())
                .baseTaxRate(request.getBaseTaxRate())
                .benefitRate(request.getBenefitRate())
                .deductionRate(request.getDeductionRate())
                .build();
    }

    private PayGroupResponse buildPayGroupResponse(Integer payGroupId) {
        return PayGroupResponse.builder()
                .payGroupId(payGroupId)
                .build();
    }

    private <T> T resolve(T newValue, T oldValue) {
        return newValue != null ? newValue : oldValue;
    }
}
