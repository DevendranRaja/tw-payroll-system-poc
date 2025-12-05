package com.tw.coupang.one_payroll.userauth.controller;

import com.tw.coupang.one_payroll.userauth.dto.AuthResponse;
import com.tw.coupang.one_payroll.userauth.dto.UserCreateRequest;
import com.tw.coupang.one_payroll.userauth.dto.UserLoginRequest;
import com.tw.coupang.one_payroll.userauth.service.UserAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserAuthService userAuthService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserCreateRequest request) {
        log.info("Received request to register user: {}", request);
        AuthResponse response = userAuthService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("Received login request: {}", request);
        AuthResponse response = userAuthService.login(request);
        return ResponseEntity.ok(response);
    }
}
