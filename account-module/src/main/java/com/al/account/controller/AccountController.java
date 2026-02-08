package com.al.account.controller;


import com.al.account.bean.dto.AccountDto;
import com.al.account.service.accountService.AccountService;
import com.al.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/open")
/**
 * 开户相关的操作
 */
public class AccountController {
    @Autowired
    private AccountService accountService;
    @PostMapping("/query")
    public Result query(@RequestBody @Valid AccountDto accountDto) throws Exception {
        return Result.success(accountService.queryByStoreId(accountDto));
    }
    @PostMapping("/save")
    public Result save(@RequestBody @Valid AccountDto accountDto) throws Exception {
        return Result.success(accountService.save(accountDto));
    }
    @PostMapping("/delete")
    public Result delete(@RequestBody @Valid AccountDto accountDto) throws Exception {
        return Result.success(accountService.delete(accountDto));
    }
    @PostMapping("/update")
    public Result update(@RequestBody @Valid AccountDto accountDto) throws Exception {
        return Result.success(accountService.update(accountDto));
    }

}
