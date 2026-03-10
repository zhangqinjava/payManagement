package com.al.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@ConditionalOnProperty(prefix = "xxl.job", name = "enable", havingValue = "true")
public class XxlJobConfig {
    @Autowired
    private XxlCoreConfigProperties xxlConfig;
    @Bean
    public XxlJobSpringExecutor xxlCoreConfig() {
        log.info("=============>>>>>>>>>xxl-job config init <<<<<<<<<<<<<<<<<<==============");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(xxlConfig.getAddresses());
        xxlJobSpringExecutor.setAddress(xxlConfig.getAddress());
        xxlJobSpringExecutor.setAppname(xxlConfig.getAppname());
        xxlJobSpringExecutor.setIp(xxlConfig.getIp());
        xxlJobSpringExecutor.setPort(xxlConfig.getPort());
        xxlJobSpringExecutor.setAccessToken(xxlConfig.getAccessToken());
        xxlJobSpringExecutor.setLogPath(xxlConfig.getLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(xxlConfig.getLogRetentionDays());
        return xxlJobSpringExecutor;
    }
}
