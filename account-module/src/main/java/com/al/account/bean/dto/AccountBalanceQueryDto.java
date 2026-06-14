package com.al.account.bean.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AccountBalanceQueryDto {
    @NotBlank(message = "商户号不能为空")
    private String merchantNo;
    @NotBlank(message = "账户号不能为空")
    private String accountNo;
    @NotBlank(message = "账户类型不能为空")
    private String accountType;
}
