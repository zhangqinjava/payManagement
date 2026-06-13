package com.al.fegin.account;

import com.al.bean.dto.account.*;
import com.al.bean.vo.account.*;
import com.al.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "account-module", path = "/account")
public interface AccountFeginClient {

    @PostMapping("/open/save")
    Result<AccountOpenVo> openAccount(@RequestBody AccountDto accountDto);

    @PostMapping("/open/update")
    Result<String> updateAccount(@RequestBody AccountDto accountDto);

    @PostMapping("/open/query")
    Result<List<AccountVo>> queryAccount(@RequestBody AccountDto accountDto);

    @PostMapping("/open/query/balance")
    Result<AccountBalanceVo> queryBalance(@RequestBody AccountBalanceQueryDto dto);

    @GetMapping("/open/listByMerchant")
    Result<List<AccountVo>> listByMerchant(@RequestParam("merchantNo") String merchantNo,
                                           @RequestParam(value = "accountType", required = false) String accountType);

    @PostMapping("/operation/up")
    Result<AccountUpDownVo> up(@RequestBody AccountUpDownDto accountUpDownDto);

    @PostMapping("/operation/down")
    Result<AccountUpDownVo> down(@RequestBody AccountUpDownDto accountUpDownDto);

    @PostMapping("/operation/downway")
    Result<AccountUpDownVo> downway(@RequestBody AccountUpDownDto accountUpDownDto);

    @PostMapping("/operation/transitdown")
    Result<AccountUpDownVo> transitdown(@RequestBody AccountUpDownDto accountUpDownDto);

    @PostMapping("/operation/transitup")
    Result<AccountUpDownVo> transitup(@RequestBody AccountUpDownDto accountUpDownDto);

    @PostMapping("/operation/transfer")
    Result<AccountTransferVo> transfer(@RequestBody AccountTransferDto accountTransferDto);

    @PostMapping("/operation/freeze")
    Result<AccountFreezeResultVo> freeze(@RequestBody AccountFreezeDto accountFreezeDto);

    @PostMapping("/operation/unfreeze")
    Result<AccountFreezeResultVo> unfreeze(@RequestBody AccountFreezeDto accountFreezeDto);

    @PostMapping("/operation/query/detail")
    Result<List<AccountQueryDtlVo>> queryFlowDetail(@RequestBody AccountQueryDto accountQueryDto);

    @PostMapping("/operation/query/flow")
    Result<AccountFlowVo> queryFlow(@RequestBody AccountFlowQueryDto dto);

    @PostMapping("/operation/query/summary")
    Result<AccountSummaryVo> querySummary(@RequestBody QuerySummaryDto dto);
}
