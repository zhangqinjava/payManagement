package com.al.common.util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;

@Slf4j
public class RestTemplateUtil {

    /* ==================== GET ==================== */

    public <T> T get(RestTemplate restTemplate,String url, Class<T> responseType) {
        return get(url, null, null, responseType,restTemplate);
    }

    public <T> T get(
            String url,
            Map<String, ?> queryParams,
            Map<String, String> headers,
            Class<T> responseType,
            RestTemplate restTemplate) {

        try {
            String fullUrl = buildUrl(url, queryParams);

            HttpEntity<Void> entity = new HttpEntity<>(buildHeaders(headers));

            ResponseEntity<T> resp = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.GET,
                    entity,
                    responseType
            );
            return resp.getBody();
        } catch (RestClientException e) {
            log.error("HTTP GET failed, url={}", url, e);
            throw new RuntimeException("HTTP GET 调用失败", e);
        }
    }

    /* ==================== POST JSON ==================== */

    public <T> T postJson(
            RestTemplate restTemplate,
            String url,
            Object body,
            Class<T> responseType) {

        return postJson(url, body, null, responseType,restTemplate);
    }

    public <T> T postJson(
            String url,
            Object body,
            Map<String, String> headers,
            Class<T> responseType,
            RestTemplate restTemplate) {

        try {
            HttpHeaders httpHeaders = buildHeaders(headers);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> entity = new HttpEntity<>(body, httpHeaders);

            ResponseEntity<T> resp = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    responseType
            );
            return resp.getBody();
        } catch (RestClientException e) {
            log.error("HTTP POST JSON failed, url={}", url, e);
            throw new RuntimeException("HTTP POST JSON 调用失败", e);
        }
    }

    /* ==================== 通用 Exchange ==================== */

    public <T> T exchange(
            String url,
            HttpMethod method,
            Object body,
            Map<String, String> headers,
            Class<T> responseType,
            RestTemplate restTemplate) {

        try {
            HttpEntity<Object> entity =
                    new HttpEntity<>(body, buildHeaders(headers));

            ResponseEntity<T> resp = restTemplate.exchange(
                    url,
                    method,
                    entity,
                    responseType
            );
            return resp.getBody();
        } catch (RestClientException e) {
            log.error("HTTP exchange failed, method={}, url={}", method, url, e);
            throw new RuntimeException("HTTP 调用失败", e);
        }
    }

    /* ==================== 工具方法 ==================== */

    private String buildUrl(String url, Map<String, ?> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return url;
        }
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(url);
        queryParams.forEach(builder::queryParam);
        return builder.toUriString();
    }

    private HttpHeaders buildHeaders(Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            httpHeaders.setAll(headers);
        }
        return httpHeaders;
    }
}
