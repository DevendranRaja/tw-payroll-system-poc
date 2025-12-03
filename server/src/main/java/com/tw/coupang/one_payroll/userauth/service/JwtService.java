package com.tw.coupang.one_payroll.userauth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    public String extractUsername(String token) {
        return "";
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return true;
    }
}
