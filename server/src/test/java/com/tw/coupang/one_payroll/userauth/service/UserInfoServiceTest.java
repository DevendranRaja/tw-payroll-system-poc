package com.tw.coupang.one_payroll.userauth.service;

import com.tw.coupang.one_payroll.userauth.entity.UserAuth;
import com.tw.coupang.one_payroll.userauth.repository.UserAuthRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserInfoServiceTest {

    @Mock
    private UserAuthRepository userAuthRepository;

    @InjectMocks
    private UserInfoService userInfoService;

    private UserAuth testUser;

    @BeforeEach
    void setUp() {
        testUser = UserAuth.builder()
                .userId("admin01")
                .password("encodedPass")
                .role(com.tw.coupang.one_payroll.userauth.enums.UserRole.ADMIN)
                .employeeId("E001")
                .build();
    }

    @Test
    void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
        when(userAuthRepository.findByUserId("admin01"))
                .thenReturn(Optional.of(testUser));

        UserDetails userDetails = userInfoService.loadUserByUsername("admin01");

        assertNotNull(userDetails);
        assertEquals("admin01", userDetails.getUsername());
        assertEquals("encodedPass", userDetails.getPassword());

        verify(userAuthRepository).findByUserId("admin01");
    }

    @Test
    void loadUserByUsernameShouldThrowExceptionWhenUserNotFound() {
        when(userAuthRepository.findByUserId("unknown"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> userInfoService.loadUserByUsername("unknown")
        );

        assertEquals("User not found: unknown", ex.getMessage());

        verify(userAuthRepository).findByUserId("unknown");
    }
}
