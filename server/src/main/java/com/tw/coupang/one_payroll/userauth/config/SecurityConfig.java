package com.tw.coupang.one_payroll.userauth.config;

import com.tw.coupang.one_payroll.userauth.enums.UserRole;
import com.tw.coupang.one_payroll.userauth.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final SecurityProperties securityProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(this::configureAuthorization)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    private void configureAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth)
    {
        var adminUrls = new java.util.HashSet<>(securityProperties.getAdminUrls());
        var employeeUrls = new java.util.HashSet<>(securityProperties.getEmployeeUrls());
        var sharedUrls = new java.util.HashSet<>(adminUrls);
        sharedUrls.retainAll(employeeUrls);
        adminUrls.removeAll(sharedUrls);
        employeeUrls.removeAll(sharedUrls);

        if (!securityProperties.getPublicUrls().isEmpty()) {
            log.info("Configuring {} public URLs", securityProperties.getPublicUrls().size());
            auth.requestMatchers(securityProperties.getPublicUrls().toArray(new String[0])).permitAll();
        }

        if (!sharedUrls.isEmpty()) {
            log.info("Configuring {} shared admin+employee URLs", sharedUrls.size());
            auth.requestMatchers(sharedUrls.toArray(new String[0]))
                    .hasAnyRole(UserRole.ADMIN.name(), UserRole.EMPLOYEE.name());
        }

        if (!adminUrls.isEmpty()) {
            log.info("Configuring {} admin URLs", adminUrls.size());
            auth.requestMatchers(adminUrls.toArray(new String[0]))
                    .hasRole(UserRole.ADMIN.name());
        }

        if (!employeeUrls.isEmpty()) {
            log.info("Configuring {} employee URLs", employeeUrls.size());
            auth.requestMatchers(employeeUrls.toArray(new String[0]))
                    .hasRole(UserRole.EMPLOYEE.name());
        }

        auth.anyRequest().authenticated();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
