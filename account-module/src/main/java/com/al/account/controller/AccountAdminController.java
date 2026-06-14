package com.al.account.controller;

import com.al.account.bean.dto.AdminAccountListDto;
import com.al.account.bean.dto.ReconcileDailyDto;
import com.al.account.service.accountService.AccountService;
import com.al.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AccountAdminController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/account/list")
    public Result accountList(@RequestBody AdminAccountListDto dto) throws Exception {
        return Result.success(accountService.adminList(dto));
    }

    @PostMapping("/reconcile/daily")
    public Result reconcileDaily(@RequestBody @Valid ReconcileDailyDto dto) throws Exception {
        return Result.success(accountService.reconcileDaily(dto));
    }
}
