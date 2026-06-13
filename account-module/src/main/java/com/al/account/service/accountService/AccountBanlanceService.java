package com.al.account.service.accountService;

import com.al.account.bean.dto.*;
import com.al.account.bean.vo.*;

import java.util.List;

public interface AccountBanlanceService {
    AccountUpDownVo up(AccountUpDownDto accountUpDownDto) throws Exception;
    AccountUpDownVo down(AccountUpDownDto accountUpDownDto) throws Exception;
    AccountTransferVo transfer(AccountTransferDto accountTransferDto) throws Exception;
    AccountFreezeResultVo freeze(AccountFreezeDto accountFreezeDto) throws Exception;
    AccountFreezeResultVo unfreeze(AccountFreezeDto accountFreezeDto) throws Exception;
    AccountUpDownVo downWay(AccountUpDownDto accountUpDownDto) throws Exception;
    AccountUpDownVo transitDown(AccountUpDownDto accountUpDownDto) throws Exception;
    AccountUpDownVo transitUp(AccountUpDownDto accountUpDownDto) throws Exception;
    List<AccountQueryDtlVo> query(AccountQueryDto accountQueryDto) throws Exception;
    AccountFlowVo queryFlow(AccountFlowQueryDto dto) throws Exception;
    AccountSummaryVo querySummary(QuerySummaryDto dto) throws Exception;
    AccountTransferVo settleClear(SettleClearDto dto) throws Exception;
    AccountUpDownVo settlePayout(SettlePayoutDto dto) throws Exception;
    BatchUpResultVo batchUp(BatchUpDto dto) throws Exception;
}
