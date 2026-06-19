package com.al.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "settle.reconcile")
public class SettleReconcileProperties {
    private boolean enabled = false;
    private boolean required = false;
}
