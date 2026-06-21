package com.al.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "auth.login")
public class LoginProperties {
    private int maxFailCount = 5;
    private int lockMinutes = 30;
    private String defaultAllowedStart = "08:00:00";
    private String defaultAllowedEnd = "22:00:00";
}
