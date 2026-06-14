package com.al.account.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountSummaryVo {
    private String merchantNo;
    private String accountNo;
    private String accountType;
    private String startDate;
    private String endDate;
    private BigDecimal totalCredit;
    private BigDecimal totalDebit;
    private BigDecimal totalFreeze;
    private BigDecimal totalUnfreeze;
    private Integer transactionCount;
}
