package com.al.account.controller;

import com.al.account.bean.dto.AccountFreezeRiskDto;
import com.al.account.bean.dto.FreezeQueryDto;
import com.al.account.service.accountService.AccountFreezeService;
import com.al.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping("/freeze")
public class AccountFreezeController {

    @Autowired
    private AccountFreezeService accountFreezeService;

    @PostMapping("/risk")
    public Result risk(@RequestBody @Valid AccountFreezeRiskDto dto) throws Exception {
        log.info("risk freeze request:{}", dto);
        return Result.success(accountFreezeService.riskFreeze(dto));
    }

    @PostMapping("/query")
    public Result query(@RequestBody FreezeQueryDto dto) throws Exception {
        log.info("query freeze list:{}", dto);
        return Result.success(accountFreezeService.queryFreeze(dto));
    }

    @PostMapping("/query/detail")
    public Result queryDetail(@RequestBody FreezeQueryDto dto) throws Exception {
        log.info("query freeze detail:{}", dto);
        return Result.success(accountFreezeService.queryFreezeDetail(dto));
    }

    @PostMapping("/unfreeze")
    public Result unfreeze(@RequestBody @Valid AccountFreezeRiskDto dto) throws Exception {
        log.info("risk unfreeze request:{}", dto);
        return Result.success(accountFreezeService.riskUnfreeze(dto));
    }
}
