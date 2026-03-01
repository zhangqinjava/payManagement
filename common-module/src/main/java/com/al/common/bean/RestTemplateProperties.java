package com.al.common.bean;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
public class RestTemplateProperties {

    /** 连接超时（ms） */
    private int connectTimeout = 3000;

    /** 读取超时（ms） */
    private int readTimeout = 5000;

    /** 从连接池获取连接超时（ms） */
    private int connectionRequestTimeout = 1000;

    /** 最大连接数 */
    private int maxTotal = 200;

    /** 单路由最大连接数 */
    private int maxPerRoute = 50;
}
