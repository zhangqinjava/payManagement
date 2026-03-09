package com.al.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "xxl.job")
@Data
public class XxlCoreConfigProperties {
    /*8
    ·   连接地址
     */
    private String addresses;
    /**
     * 加密token
     */
    private String accessToken;
    /**
     * 应用名称
     */
    private String appname;
    /*
      * 执行地址
     */
    private String address;

    /**
     * ip地址
     */
    private String ip;

    /**
     * 端口
     */
    private int port;

    /**
     * 日志地址
     */
    private String logPath;

    /**
     * 日志清理周期
     */
    private int logRetentionDays;
}
