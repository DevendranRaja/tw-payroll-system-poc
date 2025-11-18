package com.tw.coupang.one_payroll.paygroups.service;

import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupCreateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupResponse;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.repository.PayGroupRepository;
import org.springframework.stereotype.Service;

@Service
public class PayGroupServiceImpl implements PayGroupService {

    private final PayGroupRepository payGroupRepository;

    public PayGroupServiceImpl(PayGroupRepository payGroupRepository) {
        this.payGroupRepository = payGroupRepository;
    }

    @Override
    public PayGroupResponse create(PayGroupCreateRequest request) {

        PayGroup payGroup = PayGroup.builder()
                .groupName(request.getGroupName())
                .paymentCycle(request.getPaymentCycle())
                .baseTaxRate(request.getBaseTaxRate())
                .benefitRate(request.getBenefitRate())
                .deductionRate(request.getDeductionRate())
                .build();

        PayGroup savedPayGroup = payGroupRepository.save(payGroup);

        return PayGroupResponse.builder()
                .payGroupId(savedPayGroup.getId())
                .build();
    }
}
