package com.tw.coupang.one_payroll.paygroups.service;

import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupCreateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupResponse;

public interface PayGroupService {
    PayGroupResponse create(PayGroupCreateRequest request);
}
