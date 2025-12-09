package com.tw.coupang.one_payroll.userauth.filter;

import com.tw.coupang.one_payroll.common.constants.SecurityConstants;
import com.tw.coupang.one_payroll.userauth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private UserDetails mockUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        mockUser = User.builder()
                .username("admin01")
                .password("pass")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void shouldSkipAuthenticationForAuthEndpoints() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/api/auth/login");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void shouldNotAuthenticateWhenHeaderMissing() throws Exception {
        when(request.getServletPath()).thenReturn("/api/secure");
        when(request.getHeader(SecurityConstants.AUTHORIZATION)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void shouldNotAuthenticateWhenHeaderInvalid() throws Exception {
        when(request.getServletPath()).thenReturn("/api/secure");
        when(request.getHeader(SecurityConstants.AUTHORIZATION)).thenReturn("Invalid");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void shouldAuthenticateWhenTokenIsValid() throws Exception {
        when(request.getServletPath()).thenReturn("/api/secure");
        when(request.getHeader(SecurityConstants.AUTHORIZATION))
                .thenReturn("Bearer valid.jwt.token");
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("admin01");
        when(userDetailsService.loadUserByUsername("admin01")).thenReturn(mockUser);
        when(jwtService.isTokenValid("valid.jwt.token", mockUser)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth instanceof UsernamePasswordAuthenticationToken;
        assert auth.getName().equals("admin01");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsInvalid() throws Exception {
        when(request.getServletPath()).thenReturn("/api/secure");
        when(request.getHeader(SecurityConstants.AUTHORIZATION))
                .thenReturn("Bearer invalid.jwt.token");
        when(jwtService.extractUsername("invalid.jwt.token")).thenReturn("admin01");
        when(userDetailsService.loadUserByUsername("admin01")).thenReturn(mockUser);
        when(jwtService.isTokenValid("invalid.jwt.token", mockUser)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assert SecurityContextHolder.getContext().getAuthentication() == null;
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldContinueFilterChainWhenExceptionThrown() throws Exception {
        when(request.getServletPath()).thenReturn("/api/secure");
        when(request.getHeader(SecurityConstants.AUTHORIZATION))
                .thenReturn("Bearer token");
        when(jwtService.extractUsername("token")).thenThrow(new RuntimeException("err"));

        filter.doFilterInternal(request, response, filterChain);

        assert SecurityContextHolder.getContext().getAuthentication() == null;
        verify(filterChain).doFilter(request, response);
    }
}
