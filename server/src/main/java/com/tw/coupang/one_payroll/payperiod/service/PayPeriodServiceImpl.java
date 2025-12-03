package com.tw.coupang.one_payroll.payperiod.service;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import com.tw.coupang.one_payroll.paygroups.validator.PayGroupValidator;
import com.tw.coupang.one_payroll.payperiod.dto.request.PayPeriodCreateRequest;
import com.tw.coupang.one_payroll.payperiod.dto.response.PayPeriodResponse;
import com.tw.coupang.one_payroll.payperiod.entity.PayPeriod;
import com.tw.coupang.one_payroll.payperiod.exception.OverlappingPayPeriodException;
import com.tw.coupang.one_payroll.payperiod.repository.PayPeriodRepository;
import com.tw.coupang.one_payroll.payperiod.validator.PayPeriodCycleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
public class PayPeriodServiceImpl implements PayPeriodService {

    private final PayPeriodRepository payPeriodRepository;
    private final PayGroupValidator payGroupValidator;
    private final PayPeriodCycleValidator calculatorValidator;

    public PayPeriodServiceImpl(PayPeriodRepository payPeriodRepository,
                                PayGroupValidator payGroupValidator,
                                PayPeriodCycleValidator calculatorValidator) {
        this.payPeriodRepository = payPeriodRepository;
        this.payGroupValidator = payGroupValidator;
        this.calculatorValidator = calculatorValidator;
    }

    @Transactional
    @Override
    public PayPeriodResponse create(PayPeriodCreateRequest request) {
        final LocalDate startDate = request.getPayPeriod().getStartDate();
        final LocalDate endDate = request.getPayPeriod().getEndDate();

        log.info("Validating pay group existence for id={}", request.getPayGroupId());
        PayGroup payGroup = payGroupValidator.validatePayGroupExists(request.getPayGroupId());

        log.debug("Validating pay period cycle for payGroupId={}", payGroup.getId());
        calculatorValidator.validatePayPeriodAgainstPayGroup(startDate, endDate, payGroup);

        checkOverlap(payGroup.getId(), startDate, endDate);

        PayPeriod payPeriod = savePayPeriod(payGroup, startDate, endDate);

        log.info("Saved PayPeriod id={} for payGroupId={}", payPeriod.getId(), payGroup.getId());

        return PayPeriodResponse.builder()
                .id(payPeriod.getId())
                .build();
    }

    private void checkOverlap(Integer payGroupId, LocalDate start, LocalDate end) {
        boolean exists = payPeriodRepository.existsOverlappingPeriod(payGroupId, start, end);
        if (exists) {
            throw new OverlappingPayPeriodException("Pay period overlaps with existing period(s) for payGroupId=" + payGroupId);
        }
    }

    private PayPeriod savePayPeriod(PayGroup payGroup, LocalDate startDate, LocalDate endDate) {
        return payPeriodRepository.save(
                PayPeriod.builder()
                        .payGroupId(payGroup.getId())
                        .periodStartDate(startDate)
                        .periodEndDate(endDate)
                        .range(computeRange(startDate, endDate, payGroup.getPaymentCycle()))
                        .build()
        );
    }

    private String computeRange(LocalDate start, LocalDate end, PaymentCycle cycle) {
        return switch (cycle) {
            case MONTHLY -> start.getMonth().name().substring(0,3) + "-" + start.getYear();
            case WEEKLY, BIWEEKLY -> String.format("%02d-%02d %s%02d",
                    start.getDayOfMonth(), end.getDayOfMonth(),
                    start.getMonth().name().substring(0,3),
                    start.getYear() % 100);
        };
    }
}
