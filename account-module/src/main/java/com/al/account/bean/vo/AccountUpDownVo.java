package com.al.account.bean.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AccountUpDownVo {
    private String accountNo;
    private String flowNo;
    private String funCode;
    private String channel_code;
    private String funDirection;
    private String bizType;
    private BigDecimal amount;
    private BigDecimal curBalance;
    private String accountType;
}
