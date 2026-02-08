package com.al.account.service.accountService;

import com.al.account.bean.dto.AccountDto;
import com.al.account.bean.vo.AccountVo;
import com.al.common.exception.BusinessException;

import java.util.List;

public interface AccountService {
    String save(AccountDto accountDto) throws Exception;
    String update(AccountDto accountDto) throws Exception;
    String delete(AccountDto accountDto) throws Exception;
    List<AccountVo> query(AccountDto accountDto)  throws Exception;
}
