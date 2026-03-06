package com.al.common.config;

import lombok.Data;

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
