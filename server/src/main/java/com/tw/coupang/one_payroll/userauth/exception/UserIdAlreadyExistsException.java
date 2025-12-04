package com.tw.coupang.one_payroll.userauth.exception;

public class UserIdAlreadyExistsException extends RuntimeException {
    public UserIdAlreadyExistsException(String message) {
        super(message);
    }
}
