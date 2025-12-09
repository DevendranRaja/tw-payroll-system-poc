package com.tw.coupang.one_payroll.userauth.service;


import com.tw.coupang.one_payroll.userauth.exception.JwtTokenParsingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest
{
    @InjectMocks
    private JwtService jwtService;

    private UserDetails employeeTestUser;
    private String testToken;

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

        testToken = generateToken();
    }

    private String generateToken() {
        return jwtService.generateToken("User1", "E001", "EMPLOYEE");
    }

    @Test
    void generateTokenShouldIncludeClaimsAndUsername() {

        assertNotNull(testToken);

        String username = jwtService.extractUsername(testToken);
        String employeeId = jwtService.extractEmployeeId(testToken);
        String role = jwtService.extractRole(testToken);

        assertEquals("User1", username);
        assertEquals("E001", employeeId);
        assertEquals("EMPLOYEE", role);
    }

    @Test
    void isTokenValidShouldReturnFalseForInvalidUsername() {

        UserDetails otherUser = User.builder()
                .username("User2")
                .password("password")
                .roles("EMPLOYEE")
                .build();
        assertFalse(jwtService.isTokenValid(testToken, otherUser));
    }

    @Test
    void extractAllClaimsShouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid.token.value";
        assertThrows(JwtTokenParsingException.class, () -> jwtService.extractUsername(invalidToken));
    }

    @Test
    void tokenShouldExpireWhenExpirationIsGreaterThanZero() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 100);
        String validToken = generateToken();
        String expiredToken = forceTokenExpired(validToken);
        assertFalse(jwtService.isTokenValid(expiredToken, employeeTestUser));
    }

    @Test
    void tokenShouldNeverExpireWhenExpirationIsNegativeAndReturnTrue()
    {
        assertTrue(jwtService.isTokenValid(testToken, employeeTestUser));
    }
    private String forceTokenExpired(String token) {
        String[] parts = token.split("\\.");
        String header = parts[0];
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

        payload = payload.replaceFirst("\"exp\":\\d+", "\"exp\":1");

        String newPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes());

        return header + "." + newPayload + "." + parts[2];
    }

}
