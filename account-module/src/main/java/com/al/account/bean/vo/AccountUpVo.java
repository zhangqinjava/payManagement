package com.al.account.bean.vo;

import com.al.account.bean.dto.AccountDto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountUpVo {
    private String accountNo;
    private String flowNo;
    private String funCode;
    private String channel_code;
    private String funDirection;
    private String bizType;
    private BigDecimal amount;
    private BigDecimal curAmount;
    private String accountType;
}
