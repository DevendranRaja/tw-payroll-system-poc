package com.tw.coupang.one_payroll.error.validation;

import com.tw.coupang.one_payroll.error.enums.ErrorCode;
import com.tw.coupang.one_payroll.error.response.ErrorResponse;
import io.micrometer.common.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ValidationManager
{
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    public static void validateMandatoryField(String moduleName, String employeeId, List<String> fieldValues)
    {
        for(String value : fieldValues)
        {
            if (StringUtils.isEmpty(value))
            {
                throw new ValidationException(new ErrorResponse(
                        moduleName,
                        employeeId,
                        ErrorCode.MISSING_MANDATORY_FIELD.getErrorMessage(),
                        LocalDateTime.now()
                ));
            }
        }
    }

    public static void validateEmail(String moduleName, String employeeId, String email)
    {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches())
        {
            throw new ValidationException(new ErrorResponse(
                    moduleName,
                    employeeId,
                    ErrorCode.INVALID_EMAIL.getErrorMessage(),
                    LocalDateTime.now()
            ));
        }

    }

    public static void validateDate(String moduleName, String employeeId, LocalDate date)
    {
        if(date == null || date.isAfter(LocalDate.now()))
        {
            throw new ValidationException(new ErrorResponse(
                    moduleName,
                    employeeId,
                    ErrorCode.INVALID_DATE.getErrorMessage(),
                    LocalDateTime.now()
            ));
        }

    }

    public static void validateWorkingHours(String moduleName, String employeeId, LocalDate startDate, LocalDate endDate)
    {
        if(startDate == null || endDate == null)
        {
            throw new ValidationException(new ErrorResponse(
                    moduleName,
                    employeeId,
                    ErrorCode.MISSING_PAY_PERIOD.getErrorMessage(),
                    LocalDateTime.now()
            ));
        }
        long workingHours = ChronoUnit.HOURS.between(startDate.atStartOfDay(), endDate.atStartOfDay());

        if(workingHours < 0)
        {
            throw new ValidationException(new ErrorResponse(
                    moduleName,
                    employeeId,
                    ErrorCode.NEGATIVE_WORKING_HOURS.getErrorMessage(),
                    LocalDateTime.now()
            ));
        }
    }

    public static void validateNonNegativeBigDecimalValues(String moduleName, String employeeId, BigDecimal fieldValue, String fieldName)
    {
        if(fieldValue == null || fieldValue.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ValidationException(new ErrorResponse(
                    moduleName,
                    employeeId,
                    ErrorCode.NEGATIVE_VALUE.getErrorMessage() + fieldName,
                    LocalDateTime.now()
            ));
        }
    }

    public static void validateEnum(String moduleName, String employeeId, Class<? extends Enum<?>> enumClass, String fieldValue, String fieldName)
    {
        boolean valid = Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(e -> e.name().equals(fieldValue));
        if(!valid)
        {
            throw new ValidationException(new ErrorResponse(
                    moduleName,
                    employeeId,
                    ErrorCode.INVALID_VALUE.getErrorMessage() + fieldName,
                    LocalDateTime.now()
            ));
        }

    }
}
