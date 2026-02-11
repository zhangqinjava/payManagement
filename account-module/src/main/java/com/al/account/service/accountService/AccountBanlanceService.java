package com.al.account.service.accountService;

import com.al.account.bean.dto.AccountFreezeDto;
import com.al.account.bean.dto.AccountTransferDto;
import com.al.account.bean.dto.AccountUpDownDto;
import com.al.account.bean.vo.AccountFreezeResultVo;
import com.al.account.bean.vo.AccountTransferVo;
import com.al.account.bean.vo.AccountUpDownVo;

public interface AccountBanlanceService {
    AccountUpDownVo up(AccountUpDownDto accountUpDownDto) throws Exception;
    AccountUpDownVo down(AccountUpDownDto accountUpDownDto) throws Exception;
    AccountTransferVo transfer(AccountTransferDto accountTransferDto) throws Exception;
    AccountFreezeResultVo freeze(AccountFreezeDto accountFreezeDto) throws Exception;
    AccountFreezeResultVo unfreeze(AccountFreezeDto accountFreezeDto) throws Exception;
    AccountUpDownVo downWay(AccountUpDownDto accountUpDownDto) throws Exception;
    AccountUpDownVo transitDown(AccountUpDownDto accountUpDownDto) throws Exception;
    AccountUpDownVo transitUp(AccountUpDownDto accountUpDownDto) throws Exception;

}
