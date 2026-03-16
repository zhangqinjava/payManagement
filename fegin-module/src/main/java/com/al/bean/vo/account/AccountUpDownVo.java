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
