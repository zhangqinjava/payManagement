package com.al.account.service.accountService;

import com.al.account.bean.dto.AccountFreezeDto;
import com.al.account.bean.dto.AccountFreezeRiskDto;
import com.al.account.bean.dto.FreezeQueryDto;
import com.al.account.bean.vo.AccountFreezeResultVo;
import com.al.account.bean.vo.AccountFreezeVo;
import com.al.account.bean.vo.AccountfreezeDetailVo;

import java.util.List;

public interface AccountFreezeService {
    AccountFreezeResultVo freeze(AccountFreezeDto accountFreezeDto) throws Exception;
    AccountFreezeResultVo unfreeze(AccountFreezeDto accountFreezeDto) throws Exception;
    AccountFreezeResultVo riskFreeze(AccountFreezeRiskDto dto) throws Exception;
    AccountFreezeResultVo riskUnfreeze(AccountFreezeRiskDto dto) throws Exception;
    List<AccountFreezeVo> queryFreeze(FreezeQueryDto dto) throws Exception;
    List<AccountfreezeDetailVo> queryFreezeDetail(FreezeQueryDto dto) throws Exception;
}
