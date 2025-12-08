package com.tw.coupang.one_payroll.userauth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "security")
@Data
public class SecurityProperties
{
    private List<String> publicUrls = new ArrayList<>();
    private List<String> adminUrls = new ArrayList<>();
    private List<String> employeeUrls = new ArrayList<>();
}
