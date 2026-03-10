package com.al.account.service.accountService;

import com.al.account.bean.dto.AccountFreezeDto;
import com.al.account.bean.vo.AccountFreezeResultVo;

public interface AccountFreezeService {
    AccountFreezeResultVo freeze(AccountFreezeDto accountFreezeDto) throws Exception;
    AccountFreezeResultVo unfreeze(AccountFreezeDto accountFreezeDto) throws Exception;
}
