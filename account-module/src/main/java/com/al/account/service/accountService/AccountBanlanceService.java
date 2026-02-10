package com.al.account.service.accountService;

import com.al.account.bean.dto.AccountTransferDto;
import com.al.account.bean.dto.AccountUpDownDto;
import com.al.account.bean.vo.AccountTransferVo;
import com.al.account.bean.vo.AccountUpDownVo;

public interface AccountBanlanceService {
    AccountUpDownVo up(AccountUpDownDto accountUpDownDto) throws Exception;
    AccountUpDownVo down(AccountUpDownDto accountUpDownDto) throws Exception;
    AccountTransferVo transfer(AccountTransferDto accountTransferDto) throws Exception;
}
