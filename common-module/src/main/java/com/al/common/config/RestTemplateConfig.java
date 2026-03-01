package com.al.common.config;

import com.al.common.bean.RestTemplateProperties;
import lombok.RequiredArgsConstructor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {
    private  RestTemplateProperties properties=new RestTemplateProperties();

    /**
     * RestTemplate
     */
    @Bean(name = "customRestTemplate")
    public RestTemplate restTemplate(ClientHttpRequestFactory requestFactory) {
        return new RestTemplate(requestFactory);
    }

    /**
     * 请求工厂
     */
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory(
            CloseableHttpClient httpClient) {

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();

        factory.setHttpClient(httpClient);
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        factory.setConnectionRequestTimeout(
                properties.getConnectionRequestTimeout());

        return factory;
    }

    /**
     * HttpClient
     */
    @Bean
    public CloseableHttpClient httpClient(
            PoolingHttpClientConnectionManager connectionManager) {

        return HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                // 空闲连接回收
                .evictIdleConnections(30, TimeUnit.SECONDS)
                .evictExpiredConnections()
                .build();
    }

    /**
     * 连接池
     */
    @Bean
    public PoolingHttpClientConnectionManager connectionManager() {

        PoolingHttpClientConnectionManager cm =
                new PoolingHttpClientConnectionManager();

        cm.setMaxTotal(properties.getMaxTotal());
        cm.setDefaultMaxPerRoute(properties.getMaxPerRoute());
        // 防止拿到已失效连接（非常重要）
        cm.setValidateAfterInactivity(2000);

        return cm;
    }
}