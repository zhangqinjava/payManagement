package com.al.account.controller;

import com.al.account.bean.dto.*;
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

    @PostMapping("/downway")
    public Result downway(@RequestBody @Valid AccountUpDownDto accountUpDownDto) throws Exception {
        return Result.success(accountBanlanceService.downWay(accountUpDownDto));
    }

    @PostMapping("/transitdown")
    public Result transitDown(@RequestBody @Valid AccountUpDownDto accountUpDownDto) throws Exception {
        return Result.success(accountBanlanceService.transitDown(accountUpDownDto));
    }

    @PostMapping("/transitup")
    public Result transitUp(@RequestBody @Valid AccountUpDownDto accountUpDownDto) throws Exception {
        return Result.success(accountBanlanceService.transitUp(accountUpDownDto));
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

    @PostMapping("/query/detail")
    public Result queryDetail(@RequestBody @Valid AccountQueryDto accountDto) throws Exception {
        return Result.success(accountBanlanceService.query(accountDto));
    }

    @PostMapping("/query/flow")
    public Result queryFlow(@RequestBody @Valid AccountFlowQueryDto dto) throws Exception {
        return Result.success(accountBanlanceService.queryFlow(dto));
    }

    @PostMapping("/query/summary")
    public Result querySummary(@RequestBody @Valid QuerySummaryDto dto) throws Exception {
        return Result.success(accountBanlanceService.querySummary(dto));
    }

    @PostMapping("/settle/clear")
    public Result settleClear(@RequestBody @Valid SettleClearDto dto) throws Exception {
        return Result.success(accountBanlanceService.settleClear(dto));
    }

    @PostMapping("/settle/payout")
    public Result settlePayout(@RequestBody @Valid SettlePayoutDto dto) throws Exception {
        return Result.success(accountBanlanceService.settlePayout(dto));
    }

    @PostMapping("/batch/up")
    public Result batchUp(@RequestBody @Valid BatchUpDto dto) throws Exception {
        return Result.success(accountBanlanceService.batchUp(dto));
    }
}
