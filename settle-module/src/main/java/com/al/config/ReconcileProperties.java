package com.al.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "reconcile")
public class ReconcileProperties {
    private String channelCode = "DEFAULT";
    private String parseScriptCode = "DEFAULT_CSV_PARSE";
    private String compareScriptCode = "DEFAULT_ORDER_COMPARE";
}
