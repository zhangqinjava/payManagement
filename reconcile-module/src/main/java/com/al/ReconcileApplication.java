package com.al;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.al.fegin")
@MapperScan("com.al.reconcile.mapper")
public class ReconcileApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReconcileApplication.class, args);
    }
}
