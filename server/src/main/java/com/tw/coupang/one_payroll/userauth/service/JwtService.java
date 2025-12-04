package com.tw.coupang.one_payroll.userauth.service;

import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;

@Service
public class JwtService {

    private static final String SECRET_KEY = "my-very-secure-and-long-secret-key-for-jwt-123456!";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;

    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return "";
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return true;
    }
}
