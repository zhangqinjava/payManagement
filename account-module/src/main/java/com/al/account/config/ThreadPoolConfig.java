package com.al.account.config;

import org.apache.ibatis.type.Alias;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AliasFor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {
    private int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    @Bean("accountThreadPool")
    public ThreadPoolExecutor threadPoolExecutor() {
        ThreadPoolExecutor threadPoolExecutor=new ThreadPoolExecutor(CPU_COUNT
                ,CPU_COUNT*2
                , 3
                , TimeUnit.SECONDS
                ,new LinkedBlockingQueue<>(200));
        return threadPoolExecutor;

    }
}
