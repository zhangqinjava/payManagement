package com.al.settle.service.impl;

import com.al.bean.dto.account.AccountBalanceQueryDto;
import com.al.bean.dto.account.AccountQueryDto;
import com.al.bean.dto.account.QuerySummaryDto;
import com.al.bean.vo.account.AccountBalanceVo;
import com.al.bean.vo.account.AccountQueryDtlVo;
import com.al.bean.vo.account.AccountSummaryVo;
import com.al.bean.vo.merchant.MerchantAccountBindVo;
import com.al.common.Result;
import com.al.common.ResultEnum;
import com.al.common.business.BusiEnum;
import com.al.common.exception.BusinessException;
import com.al.fegin.account.AccountFeginClient;
import com.al.fegin.merchant.MerchantFeginClient;
import com.al.settle.dto.AccountSettleSnapshot;
import com.al.settle.service.AccountSettleDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class AccountSettleDataServiceImpl implements AccountSettleDataService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private AccountFeginClient accountFeginClient;
    @Autowired
    private MerchantFeginClient merchantFeginClient;

    @Override
    public AccountSettleSnapshot fetchSettleData(String merchantNo, LocalDate startDate, LocalDate endDate) throws Exception {
        MerchantAccountBindVo cashAccount = resolveCashAccount(merchantNo);
        String start = startDate.format(DATE_FMT);
        String end = endDate.format(DATE_FMT);

        AccountSummaryVo summary = querySummary(merchantNo, cashAccount, start, end);
        AccountBalanceVo balance = queryBalance(merchantNo, cashAccount);
        List<AccountQueryDtlVo> details = queryDetails(merchantNo, cashAccount, start, end);

        BigDecimal totalCredit = defaultZero(summary.getTotalCredit());
        BigDecimal totalDebit = defaultZero(summary.getTotalDebit());
        BigDecimal totalFee = totalDebit;
        BigDecimal netAmount = totalCredit.subtract(totalFee);

        return AccountSettleSnapshot.builder()
                .merchantNo(merchantNo)
                .accountNo(cashAccount.getAccountNo())
                .accountType(cashAccount.getAccountType())
                .startDate(start)
                .endDate(end)
                .totalCredit(totalCredit)
                .totalDebit(totalDebit)
                .totalFee(totalFee)
                .netAmount(netAmount)
                .transactionCount(summary.getTransactionCount() == null ? details.size() : summary.getTransactionCount())
                .summary(summary)
                .balance(balance)
                .details(details)
                .build();
    }

    private MerchantAccountBindVo resolveCashAccount(String merchantNo) throws Exception {
        Result<List<MerchantAccountBindVo>> bindResult = merchantFeginClient.listByMerchant(
                merchantNo, BusiEnum.CASH.getCode());
        if (!isSuccess(bindResult) || CollectionUtils.isEmpty(bindResult.getData())) {
            throw new BusinessException("商户未绑定现金账户，无法抽取账务数据");
        }
        return bindResult.getData().get(0);
    }

    private AccountSummaryVo querySummary(String merchantNo, MerchantAccountBindVo account,
                                           String startDate, String endDate) {
        QuerySummaryDto dto = new QuerySummaryDto();
        dto.setMerchantNo(merchantNo);
        dto.setAccountNo(account.getAccountNo());
        dto.setAccountType(account.getAccountType());
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        Result<AccountSummaryVo> result = accountFeginClient.querySummary(dto);
        if (!isSuccess(result) || result.getData() == null) {
            return AccountSummaryVo.builder()
                    .merchantNo(merchantNo)
                    .accountNo(account.getAccountNo())
                    .accountType(account.getAccountType())
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalCredit(BigDecimal.ZERO)
                    .totalDebit(BigDecimal.ZERO)
                    .totalFreeze(BigDecimal.ZERO)
                    .totalUnfreeze(BigDecimal.ZERO)
                    .transactionCount(0)
                    .build();
        }
        return result.getData();
    }

    private AccountBalanceVo queryBalance(String merchantNo, MerchantAccountBindVo account) {
        AccountBalanceQueryDto dto = new AccountBalanceQueryDto();
        dto.setMerchantNo(merchantNo);
        dto.setAccountNo(account.getAccountNo());
        dto.setAccountType(account.getAccountType());
        Result<AccountBalanceVo> result = accountFeginClient.queryBalance(dto);
        return isSuccess(result) ? result.getData() : null;
    }

    private List<AccountQueryDtlVo> queryDetails(String merchantNo, MerchantAccountBindVo account,
                                                 String startDate, String endDate) {
        AccountQueryDto dto = new AccountQueryDto();
        dto.setMerchantNo(merchantNo);
        dto.setAccountNo(account.getAccountNo());
        dto.setAcctType(account.getAccountType());
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        Result<List<AccountQueryDtlVo>> result = accountFeginClient.queryFlowDetail(dto);
        if (!isSuccess(result) || CollectionUtils.isEmpty(result.getData())) {
            return Collections.emptyList();
        }
        return result.getData();
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isSuccess(Result<?> result) {
        return result != null && result.getCode() == ResultEnum.SUCESS.getCode();
    }
}
