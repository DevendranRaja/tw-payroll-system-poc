package com.tw.coupang.one_payroll.paygroups.service;

import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupCreateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupResponse;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import com.tw.coupang.one_payroll.paygroups.repository.PayGroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class PayGroupServiceImplTest {

    @Mock
    private PayGroupRepository payGroupRepository;

    @InjectMocks
    private PayGroupServiceImpl payGroupService;

    @Test
    void create_ShouldSaveAndReturnResponse() {
        PayGroupCreateRequest request = PayGroupCreateRequest.builder()
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.valueOf(10.00))
                .benefitRate(BigDecimal.valueOf(5.00))
                .deductionRate(BigDecimal.valueOf(2.50))
                .build();

        PayGroup expected = PayGroup.builder()
                .id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.valueOf(10.00))
                .benefitRate(BigDecimal.valueOf(5.00))
                .deductionRate(BigDecimal.valueOf(2.50))
                .createdAt(LocalDateTime.now())
                .build();

        when(payGroupRepository.save(any(PayGroup.class))).thenReturn(expected);

        PayGroupResponse actual = payGroupService.create(request);

        assertThat(actual).isNotNull();
        assertThat(actual.getPayGroupId()).isEqualTo(expected.getId());
    }

    @Test
    void create_WithNullOptionalFields_ShouldUseDefaults() {
        PayGroupCreateRequest request = PayGroupCreateRequest.builder()
                .groupName("HR")
                .paymentCycle(PaymentCycle.BIWEEKLY)
                .build();

        PayGroup expected = PayGroup.builder()
                .id(2)
                .groupName("HR")
                .paymentCycle(PaymentCycle.BIWEEKLY)
                .baseTaxRate(BigDecimal.valueOf(10.00))
                .benefitRate(BigDecimal.valueOf(5.00))
                .deductionRate(BigDecimal.valueOf(2.50))
                .createdAt(LocalDateTime.now())
                .build();

        when(payGroupRepository.save(any(PayGroup.class))).thenReturn(expected);

        PayGroupResponse actual = payGroupService.create(request);

        assertThat(actual).isNotNull();
        assertThat(actual.getPayGroupId()).isEqualTo(expected.getId());
    }

    @Test
    void create_WithNullGroupName_ShouldThrowException() {
        PayGroupCreateRequest request = PayGroupCreateRequest.builder()
                .groupName(null)
                .paymentCycle(PaymentCycle.WEEKLY)
                .build();

        assertThrows(Exception.class, () -> payGroupService.create(request));
    }
}
