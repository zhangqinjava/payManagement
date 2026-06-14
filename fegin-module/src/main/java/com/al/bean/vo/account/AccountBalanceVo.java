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
public class AccountBalanceVo {
    private String merchantNo;
    private String accountNo;
    private String accountType;
    private BigDecimal balance;
    private BigDecimal frozenBalance;
    private BigDecimal transitBalance;
    private BigDecimal availableBalance;
    private String accountStatus;
    private String currency;
}
