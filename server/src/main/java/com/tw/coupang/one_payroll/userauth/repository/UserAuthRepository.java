package com.tw.coupang.one_payroll.userauth.repository;

import com.tw.coupang.one_payroll.userauth.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, String> {
    Optional<UserAuth> findByUserId(String userId);
}

