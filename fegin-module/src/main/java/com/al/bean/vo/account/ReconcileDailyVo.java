package com.al.bean.vo.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReconcileDailyVo {
    private String reconcileDate;
    private String merchantNo;
    private String accountNo;
    private String accountType;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal creditAmount;
    private BigDecimal debitAmount;
    private BigDecimal freezeAmount;
    private BigDecimal unfreezeAmount;
    private Boolean balanced;
}
