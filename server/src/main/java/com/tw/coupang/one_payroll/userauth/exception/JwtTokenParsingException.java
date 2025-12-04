package com.tw.coupang.one_payroll.userauth.exception;

public class JwtTokenParsingException extends RuntimeException {
    public JwtTokenParsingException(String message) {
        super(message);
    }
}
