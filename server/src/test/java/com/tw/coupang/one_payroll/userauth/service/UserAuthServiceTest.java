package com.tw.coupang.one_payroll.userauth.service;

import com.tw.coupang.one_payroll.userauth.dto.AuthResponse;
import com.tw.coupang.one_payroll.userauth.dto.UserCreateRequest;
import com.tw.coupang.one_payroll.userauth.dto.UserLoginRequest;
import com.tw.coupang.one_payroll.userauth.entity.UserAuth;
import com.tw.coupang.one_payroll.userauth.enums.UserRole;
import com.tw.coupang.one_payroll.userauth.exception.AuthenticationException;
import com.tw.coupang.one_payroll.userauth.exception.UserIdAlreadyExistsException;
import com.tw.coupang.one_payroll.userauth.repository.UserAuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

    @InjectMocks
    private UserAuthService userAuthService;

    @Mock
    private UserAuthRepository userAuthRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private UserCreateRequest createRequest;
    private UserLoginRequest loginRequest;
    private UserAuth existingUser;

    @BeforeEach
    void setUp() {
        createRequest = UserCreateRequest.builder()
                .userId("admin01")
                .password("admin123")
                .role(UserRole.ADMIN)
                .employeeId("E001")
                .build();

        loginRequest = UserLoginRequest.builder()
                .userId("admin01")
                .password("admin123")
                .build();

        existingUser = UserAuth.builder()
                .userId("admin01")
                .password("encodedPass")
                .role(UserRole.ADMIN)
                .employeeId("E001")
                .build();
    }

    @Test
    void registerShouldCreateUserWhenUserIdNotExists() {
        when(userAuthRepository.findByUserId("admin01")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin123")).thenReturn("encodedPass");
        when(userAuthRepository.save(any(UserAuth.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(), any(), any())).thenReturn("dummy-token");

        AuthResponse response = userAuthService.register(createRequest);

        assertNotNull(response);
        assertEquals("admin01", response.getUserId());
        assertEquals("ADMIN", response.getRole());
        assertEquals("dummy-token", response.getToken());

        verify(userAuthRepository).save(any(UserAuth.class));
        verify(jwtService).generateToken("admin01", "E001", "ADMIN");
    }

    @Test
    void registerShouldThrowExceptionWhenUserIdExists() {
        when(userAuthRepository.findByUserId("admin01")).thenReturn(Optional.of(existingUser));

        UserIdAlreadyExistsException exception = assertThrows(UserIdAlreadyExistsException.class,
                () -> userAuthService.register(createRequest));

        assertEquals("UserId already exists: admin01", exception.getMessage());
        verify(userAuthRepository, never()).save(any());
    }

    @Test
    void loginShouldReturnAuthResponseWhenCredentialsValid() {
        when(userAuthRepository.findByUserId("admin01")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("admin123", "encodedPass")).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any())).thenReturn("dummy-token");

        AuthResponse response = userAuthService.login(loginRequest);

        assertNotNull(response);
        assertEquals("admin01", response.getUserId());
        assertEquals("ADMIN", response.getRole());
        assertEquals("dummy-token", response.getToken());

        verify(jwtService).generateToken("admin01", "E001", "ADMIN");
    }

    @Test
    void loginShouldThrowExceptionWhenUserNotFound() {
        when(userAuthRepository.findByUserId("admin01")).thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> userAuthService.login(loginRequest));

        assertEquals("Invalid username or password.", exception.getMessage());
    }

    @Test
    void loginShouldThrowExceptionWhenPasswordInvalid() {
        when(userAuthRepository.findByUserId("admin01")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("admin123", "encodedPass")).thenReturn(false);

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> userAuthService.login(loginRequest));

        assertEquals("Invalid username or password.", exception.getMessage());
    }
}
