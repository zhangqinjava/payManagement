package com.al.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {
    private String secret = "pay-management-auth-secret-key-2024";
    private int accessTokenMinutes = 30;
    private int refreshTokenHours = 24;
}
