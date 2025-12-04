package com.tw.coupang.one_payroll.userauth;


import com.tw.coupang.one_payroll.userauth.exception.JwtTokenParsingException;
import com.tw.coupang.one_payroll.userauth.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest
{
    @InjectMocks
    private JwtService jwtService;

    private UserDetails employeeTestUser;

    @BeforeEach
    void setUp()
    {
        String testSecret = "VGhpcyBpcyBhIHRlc3Qgc2VjcmV0IGtleSBmb3IgSldU";

        // Inject private @Value fields
        ReflectionTestUtils.setField(jwtService, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1);

        employeeTestUser = User.builder()
                .username("User1")
                .password("password")
                .roles("EMPLOYEE")
                .build();
    }

    @Test
    void generateTokenShouldIncludeClaimsAndUsername() {
        String token = jwtService.generateToken("User1", "E001", "EMPLOYEE");
        assertNotNull(token);

        String username = jwtService.extractUsername(token);
        String employeeId = jwtService.extractEmployeeId(token);
        String role = jwtService.extractRole(token);

        assertEquals("User1", username);
        assertEquals("E001", employeeId);
        assertEquals("EMPLOYEE", role);
    }

    @Test
    void isTokenValidShouldReturnTrueForValidToken() {
        String token = jwtService.generateToken("User1", "E001", "EMPLOYEE");
        assertTrue(jwtService.isTokenValid(token, employeeTestUser));
    }

    @Test
    void isTokenValidShouldReturnFalseForInvalidUsername() {
        String token = jwtService.generateToken("User1", "E001", "EMPLOYEE");

        UserDetails otherUser = User.builder()
                .username("User2")
                .password("password")
                .roles("EMPLOYEE")
                .build();
        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void extractAllClaimsShouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid.token.value";
        assertThrows(JwtTokenParsingException.class, () -> jwtService.extractUsername(invalidToken));
    }

    @Test
    void tokenShouldExpireWhenExpirationIsGreaterThanZero() throws InterruptedException
    {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 100);
        String token = jwtService.generateToken("User1", "E001", "EMPLOYEE");

        Thread.sleep(200);
        assertFalse(jwtService.isTokenValid(token, employeeTestUser));
    }

    @Test
    void tokenShouldNeverExpireWhenExpirationIsNegative()
    {
        String token = jwtService.generateToken("User1", "E001", "EMPLOYEE");
        assertTrue(jwtService.isTokenValid(token, employeeTestUser));
    }

}
