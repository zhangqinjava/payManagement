package com.al.fegin.account;

import com.al.bean.dto.account.AccountFreezeDto;
import com.al.bean.dto.account.AccountTransferDto;
import com.al.bean.dto.account.AccountUpDownDto;
import com.al.bean.vo.account.AccountFreezeResultVo;
import com.al.bean.vo.account.AccountTransferVo;
import com.al.bean.vo.account.AccountUpDownVo;

import com.al.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-module",path = "/account")
public interface AccountFeginClient {
    /**
     * 账户上账
     * @param accountUpDownDto
     * @return
     */
    @PostMapping("/operation/up")
    Result<AccountUpDownVo> up(@RequestBody AccountUpDownDto accountUpDownDto);

    /**
     * 账户下账
     * @param accountUpDownDto
     * @return
     */
    @PostMapping("/operation/down")
    Result<AccountUpDownVo> down( @RequestBody AccountUpDownDto accountUpDownDto);

    /**
     * 下账到在途
     * @param accountUpDownDto
     * @return
     */
    @PostMapping("/operation/downway")
    Result<AccountUpDownVo> downway(@RequestBody AccountUpDownDto accountUpDownDto);

    /**
     * 在途账户下账
     * @param accountUpDownDto
     * @return
     */
    @PostMapping("/operation/transitdown")
    Result<AccountUpDownVo> transitdown(@RequestBody AccountUpDownDto accountUpDownDto);
    /**
     * 在途账户上涨到总账户
     */
    @PostMapping("/operation/transitup")
    Result<AccountUpDownVo> transitup(@RequestBody AccountUpDownDto accountUpDownDto);

    /**
     * 转账
     * @param accountUpDownDto
     * @return
     */
    @PostMapping("/operation/transfer")
    Result<AccountTransferVo> transfer(@RequestBody AccountTransferDto accountUpDownDto);

    /**
     * 冻结
     * @param accountUpDownDto
     * @return
     */
    @PostMapping("/operation/freeze")
    Result<AccountFreezeResultVo> freeze(@RequestBody AccountFreezeDto accountUpDownDto);

    /**
     * 解冻
     * @param accountUpDownDto
     * @return
     */
    @PostMapping("/operation/unfreeze")
    Result<AccountFreezeResultVo> unfreeze(@RequestBody AccountFreezeDto accountUpDownDto);

    /**
     * 查询流水明细
     * @param accountUpDownDto
     * @return
     */
    @PostMapping("/operation/query/detail")
    Result<AccountFreezeResultVo> queryflowdetail(@RequestBody AccountFreezeDto accountUpDownDto);

}
