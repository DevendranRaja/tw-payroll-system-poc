package com.tw.coupang.one_payroll.userauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tw.coupang.one_payroll.common.exception.GlobalExceptionHandler;
import com.tw.coupang.one_payroll.userauth.dto.AuthResponse;
import com.tw.coupang.one_payroll.userauth.dto.UserCreateRequest;
import com.tw.coupang.one_payroll.userauth.dto.UserLoginRequest;
import com.tw.coupang.one_payroll.userauth.enums.UserRole;
import com.tw.coupang.one_payroll.userauth.exception.AuthenticationException;
import com.tw.coupang.one_payroll.userauth.exception.UserIdAlreadyExistsException;
import com.tw.coupang.one_payroll.userauth.service.UserAuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserAuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserAuthService userAuthService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        UserAuthController controller = new UserAuthController(userAuthService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    void registerSuccess() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .userId("user1")
                .password("password")
                .role(UserRole.EMPLOYEE)
                .employeeId("E001")
                .build();

        AuthResponse response = AuthResponse.builder()
                .userId("user1")
                .role("EMPLOYEE")
                .token("dummy-token")
                .build();

        when(userAuthService.register(any(UserCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.token").value("dummy-token"));

        verify(userAuthService, times(1)).register(any(UserCreateRequest.class));
    }

    @Test
    void registerFailsWhenUserIdExists() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .userId("user1")
                .password("password")
                .role(UserRole.EMPLOYEE)
                .employeeId("E001")
                .build();

        when(userAuthService.register(any(UserCreateRequest.class)))
                .thenThrow(new UserIdAlreadyExistsException("UserId already exists"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USERID_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("UserId already exists"));

        verify(userAuthService, times(1)).register(any(UserCreateRequest.class));
    }

    @Test
    void loginSuccess() throws Exception {
        UserLoginRequest request = UserLoginRequest.builder()
                .userId("user1")
                .password("password")
                .build();

        AuthResponse response = AuthResponse.builder()
                .userId("user1")
                .role("EMPLOYEE")
                .token("dummy-token")
                .build();

        when(userAuthService.login(any(UserLoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.token").value("dummy-token"));

        verify(userAuthService, times(1)).login(any(UserLoginRequest.class));
    }

    @Test
    void loginFailsWithInvalidCredentials() throws Exception {
        UserLoginRequest request = UserLoginRequest.builder()
                .userId("user1")
                .password("wrong-password")
                .build();

        when(userAuthService.login(any(UserLoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));

        verify(userAuthService, times(1)).login(any(UserLoginRequest.class));
    }
}
