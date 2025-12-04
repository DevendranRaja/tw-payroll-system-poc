package com.tw.coupang.one_payroll.userauth.service;

import com.tw.coupang.one_payroll.userauth.dto.AuthResponse;
import com.tw.coupang.one_payroll.userauth.dto.UserCreateRequest;
import com.tw.coupang.one_payroll.userauth.dto.UserLoginRequest;
import com.tw.coupang.one_payroll.userauth.entity.UserAuth;
import com.tw.coupang.one_payroll.userauth.exception.UserIdAlreadyExistsException;
import com.tw.coupang.one_payroll.userauth.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserAuthRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(UserCreateRequest request) {

        if (repository.findByUserId(request.getUserId()).isPresent()) {
            throw new UserIdAlreadyExistsException("UserId already exists: " + request.getUserId());
        }

        UserAuth newUser = UserAuth.builder()
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        repository.save(newUser);

        String token = ""
//                jwtService.generateToken(newUser.getUsername(), newUser.getRole().name())
                ;

        return new AuthResponse(newUser.getUserId(), newUser.getRole().name(), token);
    }

    public AuthResponse login(UserLoginRequest request) {
        UserAuth user = repository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Invalid username or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password.");
        }

        String token = ""
//                jwtService.generateToken(newUser.getUsername(), newUser.getRole().name())
                ;

        return new AuthResponse(user.getUserId(), user.getRole().name(), token);
    }
}
