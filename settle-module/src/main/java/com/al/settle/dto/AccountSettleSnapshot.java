package com.al.settle.dto;

import com.al.bean.vo.account.AccountQueryDtlVo;
import com.al.bean.vo.account.AccountSummaryVo;
import com.al.bean.vo.account.AccountBalanceVo;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Data
@Builder
public class AccountSettleSnapshot {
    private String merchantNo;
    private String accountNo;
    private String accountType;
    private String startDate;
    private String endDate;
    private BigDecimal totalCredit;
    private BigDecimal totalDebit;
    private BigDecimal totalFee;
    private BigDecimal netAmount;
    private Integer transactionCount;
    private AccountSummaryVo summary;
    private AccountBalanceVo balance;
    @Builder.Default
    private List<AccountQueryDtlVo> details = Collections.emptyList();
}
