package com.tw.coupang.one_payroll.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PayPeriodValidator.class)
@Documented
public @interface ValidPayPeriod {
    String message() default "Invalid pay period: endDate must not be before startDate";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
