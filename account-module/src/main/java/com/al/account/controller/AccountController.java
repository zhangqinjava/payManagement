package com.al.account.controller;

import com.al.account.bean.dto.*;
import com.al.account.service.accountService.AccountService;
import com.al.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 开户相关的操作
 */
@RestController
@RequestMapping("/open")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/query")
    public Result query(@RequestBody @Valid AccountDto accountDto) throws Exception {
        return Result.success(accountService.query(accountDto));
    }

    @PostMapping("/save")
    public Result save(@RequestBody @Valid AccountDto accountDto) throws Exception {
        return Result.success(accountService.save(accountDto));
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody AccountDto accountDto) throws Exception {
        return Result.success(accountService.delete(accountDto));
    }

    @PostMapping("/update")
    public Result update(@RequestBody @Valid AccountDto accountDto) throws Exception {
        return Result.success(accountService.update(accountDto));
    }

    @PostMapping("/query/balance")
    public Result queryBalance(@RequestBody @Valid AccountBalanceQueryDto dto) throws Exception {
        return Result.success(accountService.queryBalance(dto));
    }

    @GetMapping("/listByMerchant")
    public Result listByMerchant(@RequestParam String merchantNo,
                                 @RequestParam(required = false) String accountType) throws Exception {
        return Result.success(accountService.listByMerchant(merchantNo, accountType));
    }

    @PostMapping("/status/freeze")
    public Result freezeAccount(@RequestBody @Valid AccountStatusDto dto) throws Exception {
        return Result.success(accountService.freezeAccount(dto));
    }

    @PostMapping("/status/close")
    public Result closeAccount(@RequestBody @Valid AccountStatusDto dto) throws Exception {
        return Result.success(accountService.closeAccount(dto));
    }

    @PostMapping("/query/openFlow")
    public Result queryOpenFlow(@RequestBody AccountOpenFlowQueryDto dto) throws Exception {
        return Result.success(accountService.queryOpenFlow(dto));
    }
}
