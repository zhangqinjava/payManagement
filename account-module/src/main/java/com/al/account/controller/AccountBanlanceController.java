package com.al.account.controller;

import com.al.account.bean.dto.AccountFreezeDto;
import com.al.account.bean.dto.AccountTransferDto;
import com.al.account.bean.dto.AccountUpDownDto;
import com.al.account.service.accountService.AccountBanlanceService;
import com.al.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/operation")
public class AccountBanlanceController {
    @Autowired
    private AccountBanlanceService accountBanlanceService;
    @PostMapping("/up")
    public Result up(@RequestBody @Valid AccountUpDownDto accountUpDownDto) throws Exception {
        return Result.success(accountBanlanceService.up(accountUpDownDto));
    }
    @PostMapping("/down")
    public Result down(@RequestBody @Valid AccountUpDownDto accountUpDownDto) throws Exception {
        return Result.success(accountBanlanceService.down(accountUpDownDto));
    }
    @PostMapping("/transfer")
    public Result transfer(@RequestBody @Valid AccountTransferDto accountTransferDto) throws Exception {
        return Result.success(accountBanlanceService.transfer(accountTransferDto));
    }
    @PostMapping("/freeze")
    public Result freeze(@RequestBody @Valid AccountFreezeDto accountFreezeDto) throws Exception {
        return Result.success(accountBanlanceService.freeze(accountFreezeDto));
    }
    @PostMapping("/unfreeze")
    public Result unfreeze(@RequestBody @Valid AccountFreezeDto accountFreezeDto) throws Exception {
        return Result.success(accountBanlanceService.unfreeze(accountFreezeDto));
    }
}
