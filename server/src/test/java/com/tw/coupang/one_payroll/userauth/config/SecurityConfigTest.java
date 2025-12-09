package com.tw.coupang.one_payroll.userauth.config;

import com.tw.coupang.one_payroll.userauth.dto.AuthResponse;
import com.tw.coupang.one_payroll.userauth.service.UserAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class OverrideConfig {
        @Bean
        UserAuthService userAuthService() {
            return mock(UserAuthService.class);
        }
    }

    @Autowired
    UserAuthService userAuthService;

    @Test
    void publicUrlShouldBeAccessibleWithoutAuth() throws Exception {
        AuthResponse mockResponse = new AuthResponse(
                "admin01",
                "ADMIN",
                "dummy-token"
        );

        when(userAuthService.login(any())).thenReturn(mockResponse);

        String loginJson = """
        { "userId": "admin01", "password": "admin123" }
    """;

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                        .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("admin01"));
    }

    @Test
    void adminUrlShouldBeForbiddenForEmployee() throws Exception {
        mockMvc.perform(get("/employees")
                .with(user("user01").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }
}
