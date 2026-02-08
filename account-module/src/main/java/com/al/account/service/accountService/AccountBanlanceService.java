package com.al.account.service.accountService;

import com.al.account.bean.dto.AccountUpDto;
import com.al.account.bean.vo.AccountUpVo;

public interface AccountBanlanceService {
    AccountUpVo up(AccountUpDto accountUpDto) throws Exception;
}
