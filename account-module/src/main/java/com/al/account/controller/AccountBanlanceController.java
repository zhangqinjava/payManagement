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

    /**
     * 上涨
     * @param accountUpDownDto
     * @return
     * @throws Exception
     */
    @PostMapping("/up")
    public Result up(@RequestBody @Valid AccountUpDownDto accountUpDownDto) throws Exception {
        return Result.success(accountBanlanceService.up(accountUpDownDto));
    }

    /**
     * 下账
     * @param accountUpDownDto
     * @return
     * @throws Exception
     */
    @PostMapping("/down")
    public Result down(@RequestBody @Valid AccountUpDownDto accountUpDownDto) throws Exception {
        return Result.success(accountBanlanceService.down(accountUpDownDto));
    }

    /**
     * 将余额下账，然后上账到在途
     * @param accountUpDownDto
     * @return
     * @throws Exception
     */
    @PostMapping("/downway")
    public Result downway(@RequestBody @Valid AccountUpDownDto accountUpDownDto) throws Exception {
        return Result.success(accountBanlanceService.downWay(accountUpDownDto));
    }

    /**
     * 在途账户下账
     * @param accountUpDownDto
     * @return
     * @throws Exception
     */
    @PostMapping("/transitdown")
    public Result transitDown(@RequestBody @Valid AccountUpDownDto accountUpDownDto) throws Exception {
        return Result.success(accountBanlanceService.transitDown(accountUpDownDto));
    }
    /**
     * 在途账户上涨到总账户
     * @param accountUpDownDto
     * @return
     * @throws Exception
     */
    @PostMapping("/transitup")
    public Result transitUp(@RequestBody @Valid AccountUpDownDto accountUpDownDto) throws Exception {
        return Result.success(accountBanlanceService.transitUp(accountUpDownDto));
    }

    /**
     * 转账
     * @param accountTransferDto
     * @return
     * @throws Exception
     */
    @PostMapping("/transfer")
    public Result transfer(@RequestBody @Valid AccountTransferDto accountTransferDto) throws Exception {
        return Result.success(accountBanlanceService.transfer(accountTransferDto));
    }

    /**
     * 冻结
     * @param accountFreezeDto
     * @return
     * @throws Exception
     */
    @PostMapping("/freeze")
    public Result freeze(@RequestBody @Valid AccountFreezeDto accountFreezeDto) throws Exception {
        return Result.success(accountBanlanceService.freeze(accountFreezeDto));
    }

    /**
     * 解冻
     * @param accountFreezeDto
     * @return
     * @throws Exception
     */
    @PostMapping("/unfreeze")
    public Result unfreeze(@RequestBody @Valid AccountFreezeDto accountFreezeDto) throws Exception {
        return Result.success(accountBanlanceService.unfreeze(accountFreezeDto));
    }

}
