package com.tw.coupang.one_payroll.userauth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SecurityPropertiesTest {

    @Autowired
    private SecurityProperties securityProperties;

    @Test
    void loadsAdminUrlsFromYaml()
    {
        assertThat(securityProperties.getAdminUrls()).contains("/employee/**");
    }

    @Test
    void loadsEmployeeUrlsFromYaml()
    {
        assertThat(securityProperties.getEmployeeUrls()).contains("/payslip-ess/**");
    }

    @Test
    void loadsPublicUrlsFromYaml()
    {
        assertThat(securityProperties.getPublicUrls()).contains("/auth/**");
    }
}