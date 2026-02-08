package com.al.account.bean.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class AccountDto {
    @NotBlank(message = "请求流水号不能为空")
    private String flow;
    @NotBlank(message = "商户号不能为空")
    private String storeId;
    @NotBlank(message = "渠道号不能为空")
    private String channelCode;
    @NotBlank(message = "渠道账户号不能为空")
    private String channelAccountNo;
    @NotBlank(message = "账户类型不能为空")
    private String accountType;
    private String accountNo;
    private String currency;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal frozenBalance;
    private BigDecimal transitBalance;
    private String accountStatus;
    private String remark;
    private String operation;
    private String modifyUser;
}
