package com.al.account.service.accountService;

import com.al.account.bean.dto.AccountUpDto;
import com.al.account.bean.vo.AccountUpDownVo;

public interface AccountBanlanceService {
    AccountUpDownVo up(AccountUpDto accountUpDto) throws Exception;
    AccountUpDownVo down(AccountUpDto accountUpDto) throws Exception;
}
