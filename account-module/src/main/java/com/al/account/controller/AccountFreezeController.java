package com.al.account.controller;

import com.al.common.result.Result;
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
public class AccountFreezeController{

    @GetMapping("/risk")
    public Result risk(){
        return Result.success(null);
    }
    @GetMapping("/query")
    public Result query(){
        log.info("query freeze ");
        return Result.success(null);
    }

}
