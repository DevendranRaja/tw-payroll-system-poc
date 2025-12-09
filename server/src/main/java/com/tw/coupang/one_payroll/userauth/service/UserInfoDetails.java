package com.tw.coupang.one_payroll.userauth.service;

import com.tw.coupang.one_payroll.userauth.entity.UserAuth;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class UserInfoDetails implements UserDetails {

    private final String userId;
    private final String password;
    private final String employeeId;
    private final String role;

    public UserInfoDetails(UserAuth user) {
        this.userId = user.getUserId();
        this.password = user.getPassword();
        this.employeeId = user.getEmployeeId();
        this.role = user.getRole().name();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userId;
    }

}
