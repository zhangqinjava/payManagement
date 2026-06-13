package com.al.account.service.accountService;

import com.al.account.bean.dto.AccountDto;
import com.al.account.bean.dto.AccountBalanceQueryDto;
import com.al.account.bean.dto.AccountOpenFlowQueryDto;
import com.al.account.bean.dto.AccountStatusDto;
import com.al.account.bean.dto.AdminAccountListDto;
import com.al.account.bean.dto.ReconcileDailyDto;
import com.al.account.bean.vo.AccountOpenFlowVo;
import com.al.account.bean.vo.AccountOpenVo;
import com.al.account.bean.vo.AccountVo;
import com.al.account.bean.vo.AccountBalanceVo;
import com.al.account.bean.vo.PageVo;
import com.al.account.bean.vo.ReconcileDailyVo;
import com.al.common.exception.BusinessException;

import java.util.List;

public interface AccountService {
    AccountOpenVo save(AccountDto accountDto) throws Exception;
    String update(AccountDto accountDto) throws Exception;
    String delete(AccountDto accountDto) throws Exception;
    List<AccountVo> query(AccountDto accountDto)  throws Exception;
    AccountBalanceVo queryBalance(AccountBalanceQueryDto dto) throws Exception;
    List<AccountVo> listByMerchant(String merchantNo, String accountType) throws Exception;
    String freezeAccount(AccountStatusDto dto) throws Exception;
    String closeAccount(AccountStatusDto dto) throws Exception;
    List<AccountOpenFlowVo> queryOpenFlow(AccountOpenFlowQueryDto dto) throws Exception;
    PageVo<AccountVo> adminList(AdminAccountListDto dto) throws Exception;
    List<ReconcileDailyVo> reconcileDaily(ReconcileDailyDto dto) throws Exception;
}
