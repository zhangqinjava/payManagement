package com.al.account.controller;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

@RestController
@Slf4j
@RequestMapping("/freeze")
public class AccountFreezeController {
    @Autowired
    private RedissonClient redissonClient;
    @Resource(name = "accountThreadPool")
    private Executor asyncExecutor;
    int count=0;

    @GetMapping("/test")
    public void test(){
    }

}
