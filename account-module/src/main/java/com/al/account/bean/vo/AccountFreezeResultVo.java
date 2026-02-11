package com.al.account.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountFreezeResultVo {
    private String flowNo;
    private String accountNo;
    private String storeId;
    private String accountType;
    private String funCode;
    private String channel_code;
    private String bizType;
    private BigDecimal freezeAmount;
    private BigDecimal curBalance;
    private BigDecimal frozenBalance;
}
